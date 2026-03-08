package com.example.demo.services.search;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

public final class FilterSpecBuilder {

    private FilterSpecBuilder() {}

    public static <T> Specification<T> toSpecification(FilterExpression expr) {
        return (root, query, cb) -> toPredicate(expr, root, query, cb);
    }
    private static final Set<String> JSONB_COLUMNS = Set.of(
            "body",
            "payload",
            "metadata"
    );
    private static Predicate toPredicate(
            FilterExpression expr,
            Root<?> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb
    ) {
        if (expr == null) return cb.conjunction();

        if (expr instanceof AndExpression and) {
            Predicate[] parts = and.expressions().stream()
                    .filter(Objects::nonNull)
                    .map(e -> toPredicate(e, root, query, cb))
                    .toArray(Predicate[]::new);
            return parts.length == 0 ? cb.conjunction() : cb.and(parts);
        }

        if (expr instanceof OrExpression or) {
            Predicate[] parts = or.expressions().stream()
                    .filter(Objects::nonNull)
                    .map(e -> toPredicate(e, root, query, cb))
                    .toArray(Predicate[]::new);
            return parts.length == 0 ? cb.conjunction() : cb.or(parts);
        }

        if (expr instanceof NotExpression not) {
            return cb.not(toPredicate(not.expression(), root, query, cb));
        }

        if (expr instanceof ValueExpression v) {
            return buildValuePredicate(v, root, cb);
        }

        if (expr instanceof ValuesExpression v) {
            return buildValuesPredicate(v, root, cb);
        }

        if (expr instanceof BetweenExpression b) {
            return buildBetweenPredicate(b, root, cb);
        }

        if (expr instanceof ComboSelectionExpression c) {
            return buildComboSelectionPredicate(c, root, cb);
        }

        throw new IllegalArgumentException("Unsupported operator: " + expr.operator());
    }

    private static Path<?> resolvePath(From<?, ?> root, FieldPathDto field) {
        List<String> segs = field == null ? List.of() : field.segments();
        if (segs.isEmpty()) throw new IllegalArgumentException("field is required");

        Path<?> path = root;
        for (int i = 0; i < segs.size(); i++) {
            String seg = segs.get(i);
            boolean isLast = (i == segs.size() - 1);

            if (!isLast && path instanceof From<?, ?> from) {
                path = from.join(seg, JoinType.LEFT);
            } else {
                path = path.get(seg);
            }
        }
        return path;
    }

    private static Predicate maybeNegate(boolean exclude, Predicate predicate, CriteriaBuilder cb) {
        return exclude ? cb.not(predicate) : predicate;
    }

    private static Predicate buildValuePredicate(ValueExpression v, Root<?> root, CriteriaBuilder cb) {
        if (isJsonField(v.field())) {
            return buildJsonValuePredicate(v, root, cb);
        }

        Path<?> path = resolvePath(root, v.field());
        String op = v.operator();
        Object raw = v.value();

        Predicate p = switch (op) {
            case "equal" -> cb.equal(path, raw);
            case "greater" -> cb.greaterThan(pathAsComparable(path), asComparable(raw));
            case "greater-or-equal" -> cb.greaterThanOrEqualTo(pathAsComparable(path), asComparable(raw));
            case "less" -> cb.lessThan(pathAsComparable(path), asComparable(raw));
            case "less-or-equal" -> cb.lessThanOrEqualTo(pathAsComparable(path), asComparable(raw));

            case "like" -> buildLike(cb, path.as(String.class), Objects.toString(raw, ""), v.caseSensitive());

            case "regexp" -> {
                Expression<Boolean> expr = cb.function(
                        "regexp",
                        Boolean.class,
                        path.as(String.class),
                        cb.literal(Objects.toString(raw, ""))
                );
                yield cb.isTrue(expr);
            }

            default -> throw new IllegalArgumentException("Unsupported value operator: " + op);
        };

        return maybeNegate(v.exclude(), p, cb);
    }
    private static Predicate buildJsonValuePredicate(ValueExpression v, Root<?> root, CriteriaBuilder cb) {
        Expression<String> jsonText = resolveJsonTextPath(root, cb, v.field());
        String op = v.operator();
        Object raw = v.value();

        Predicate p = switch (op) {
            case "equal" -> cb.equal(jsonText, raw == null ? null : String.valueOf(raw));

            case "like" -> buildLike(cb, jsonText, Objects.toString(raw, ""), v.caseSensitive());

            case "greater" -> cb.greaterThan(jsonText, String.valueOf(raw));
            case "greater-or-equal" -> cb.greaterThanOrEqualTo(jsonText, String.valueOf(raw));
            case "less" -> cb.lessThan(jsonText, String.valueOf(raw));
            case "less-or-equal" -> cb.lessThanOrEqualTo(jsonText, String.valueOf(raw));

            case "regexp" -> {
                Expression<Boolean> expr = cb.function(
                        "regexp",
                        Boolean.class,
                        jsonText,
                        cb.literal(Objects.toString(raw, ""))
                );
                yield cb.isTrue(expr);
            }

            default -> throw new IllegalArgumentException("Unsupported JSON value operator: " + op);
        };

        return maybeNegate(v.exclude(), p, cb);
    }
    
    private static Predicate buildValuesPredicate(ValuesExpression v, Root<?> root, CriteriaBuilder cb) {
        if (!"in".equals(v.operator())) {
            throw new IllegalArgumentException("Unsupported values operator: " + v.operator());
        }

        CriteriaBuilder.In<Object> in;

        if (isJsonField(v.field())) {
            Expression<String> expr = resolveJsonTextPath(root, cb, v.field());
            in = cb.in(expr);
            for (Object o : Optional.ofNullable(v.values()).orElse(List.of())) {
                in.value(o == null ? null : String.valueOf(o));
            }
        } else {
            Path<?> path = resolvePath(root, v.field());
            in = cb.in(path.as(Object.class));
            for (Object o : Optional.ofNullable(v.values()).orElse(List.of())) {
                in.value(o);
            }
        }

        return maybeNegate(v.exclude(), in, cb);
    }

    private static Predicate buildBetweenPredicate(BetweenExpression b, Root<?> root, CriteriaBuilder cb) {
        if (!"between".equals(b.operator())) {
            throw new IllegalArgumentException("Unsupported between operator: " + b.operator());
        }

        Path<?> path = resolvePath(root, b.field());
        Comparable<?> from = asComparable(b.from());
        Comparable<?> to = asComparable(b.to());

        @SuppressWarnings({"unchecked", "rawtypes"})
        Predicate p = cb.between((Expression) pathAsComparable(path), (Comparable) from, (Comparable) to);

        return maybeNegate(b.exclude(), p, cb);
    }

    private static Predicate buildLike(
            CriteriaBuilder cb,
            Expression<String> expr,
            String pattern,
            Boolean caseSensitive
    ) {
        boolean cs = Boolean.TRUE.equals(caseSensitive);

        if (cs) {
            return cb.like(expr, pattern);
        }

        return cb.like(cb.lower(expr), pattern.toLowerCase(Locale.ROOT));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <Y extends Comparable> Expression<Y> pathAsComparable(Path<?> path) {
        return (Expression) path; // raw cast is intentional for Criteria API generics
    }

    private static Comparable asComparable(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Comparable c) return c; // <-- NO wildcard
        throw new IllegalArgumentException("Value is not comparable: " + raw + " (" + raw.getClass() + ")");
    }

    // ---- Combo-selection mapping (example) ----
    private static Predicate buildComboSelectionPredicate(ComboSelectionExpression c, Root<?> root, CriteriaBuilder cb) {
        Path<?> path = resolvePath(root, c.field());
        ComboSelectionValuesDto v = c.value();
        if (v == null) return cb.conjunction();

        List<Predicate> include = new ArrayList<>();
        List<Predicate> exclude = new ArrayList<>();

        for (SingleValueRowDataDto row : Optional.ofNullable(v.singleIncludeValues()).orElse(List.of())) {
            include.add(mapSingleRow(row, path, cb, false));
        }
        for (SingleValueRowDataDto row : Optional.ofNullable(v.singleExcludeValues()).orElse(List.of())) {
            exclude.add(mapSingleRow(row, path, cb, true));
        }
        for (RangeRowDataDto row : Optional.ofNullable(v.rangeIncludeValues()).orElse(List.of())) {
            include.add(mapRangeRow(row, path, cb, false));
        }
        for (RangeRowDataDto row : Optional.ofNullable(v.rangeExcludeValues()).orElse(List.of())) {
            exclude.add(mapRangeRow(row, path, cb, true));
        }

        Predicate includeP = include.isEmpty()
                ? cb.conjunction()
                : cb.or(include.toArray(Predicate[]::new));

        Predicate excludeP = exclude.isEmpty()
                ? cb.conjunction()
                : cb.and(exclude.toArray(Predicate[]::new));

        return cb.and(includeP, excludeP);
    }

    private static Predicate mapSingleRow(SingleValueRowDataDto row, Path<?> path, CriteriaBuilder cb, boolean negate) {
        String op = row.operator(); // e.g. "EQUAL", "CONTAINS"...
        Object val = row.value();

        Predicate p = switch (op) {
            case "EQUAL" -> cb.equal(path, val);
            case "NOT_EQUAL" -> cb.notEqual(path, val);

            case "CONTAINS" ->
                    cb.like(cb.lower(path.as(String.class)),
                            "%" + Objects.toString(val, "").toLowerCase(Locale.ROOT) + "%");

            case "NOT_CONTAINS" ->
                    cb.notLike(cb.lower(path.as(String.class)),
                            "%" + Objects.toString(val, "").toLowerCase(Locale.ROOT) + "%");

            case "STARTS_WITH" ->
                    cb.like(cb.lower(path.as(String.class)),
                            Objects.toString(val, "").toLowerCase(Locale.ROOT) + "%");

            case "ENDS_WITH" ->
                    cb.like(cb.lower(path.as(String.class)),
                            "%" + Objects.toString(val, "").toLowerCase(Locale.ROOT));

            case "PATTERN" -> cb.like(path.as(String.class), Objects.toString(val, "")); // expects %/_
            case "EXCLUDE_PATTERN" -> cb.notLike(path.as(String.class), Objects.toString(val, ""));

            case "GREATER_THAN" -> cb.greaterThan(pathAsComparable(path), asComparable(val));
            case "LESS_THAN" -> cb.lessThan(pathAsComparable(path), asComparable(val));
            case "GREATER_OR_EQUAL" -> cb.greaterThanOrEqualTo(pathAsComparable(path), asComparable(val));
            case "LESS_OR_EQUAL" -> cb.lessThanOrEqualTo(pathAsComparable(path), asComparable(val));

            default -> throw new IllegalArgumentException("Unsupported SingleOperator: " + op);
        };

        return negate ? cb.not(p) : p;
    }

    private static Predicate mapRangeRow(RangeRowDataDto row, Path<?> path, CriteriaBuilder cb, boolean negate) {
        String op = row.operator(); // "IN_RANGE" / "OUTSIDE_RANGE"
        Comparable<?> from = row.from() == null ? null : row.from().doubleValue();
        Comparable<?> to = row.to() == null ? null : row.to().doubleValue();

        Predicate p;
        if ("IN_RANGE".equals(op)) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Predicate between = cb.between((Expression) pathAsComparable(path), (Comparable) from, (Comparable) to);
            p = between;
        } else if ("OUTSIDE_RANGE".equals(op)) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Predicate between = cb.between((Expression) pathAsComparable(path), (Comparable) from, (Comparable) to);
            p = cb.not(between);
        } else {
            throw new IllegalArgumentException("Unsupported RangeOperator: " + op);
        }

        return negate ? cb.not(p) : p;
    }
    private static boolean isJsonField(FieldPathDto field) {
        List<String> segs = field == null ? List.of() : field.segments();
        return !segs.isEmpty() && JSONB_COLUMNS.contains(segs.get(0));
    }

    private static Expression<String> resolveJsonTextPath(
            Root<?> root,
            CriteriaBuilder cb,
            FieldPathDto field
    ) {
        List<String> segs = field == null ? List.of() : field.segments();

        if (segs.isEmpty()) {
            throw new IllegalArgumentException("field is required");
        }

        if (segs.size() < 2) {
            throw new IllegalArgumentException(
                    "JSON field path must contain at least the json column and one nested key"
            );
        }

        String jsonColumn = segs.get(0);
        Expression<?> jsonExpr = root.get(jsonColumn);

        List<Expression<?>> args = new ArrayList<>();
        args.add(jsonExpr);

        for (int i = 1; i < segs.size(); i++) {
            args.add(cb.literal(segs.get(i)));
        }

        @SuppressWarnings("unchecked")
        Expression<String> expr = cb.function(
                "jsonb_extract_path_text",
                String.class,
                args.toArray(new Expression[0])
        );

        return expr;
    }
}