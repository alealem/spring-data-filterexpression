package com.example.search.jpa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

public final class FilterSpecBuilder {

  private FilterSpecBuilder() {}

  public static <T> Specification<T> toSpecification(FilterExpression expr, SearchModel<T> model) {
    return (root, query, cb) -> toPredicate(expr, root, query, cb, model);
  }

  private static <T> Predicate toPredicate(
      FilterExpression expr,
      Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      SearchModel<T> model) {
    return switch (expr) {
      case null -> cb.conjunction();
      case AndExpression and -> combinePredicates(and.expressions(), root, query, cb, model, true);
      case OrExpression or -> combinePredicates(or.expressions(), root, query, cb, model, false);
      case NotExpression not -> cb.not(toPredicate(not.expression(), root, query, cb, model));
      case ValueExpression value -> buildValuePredicate(value, root, query, cb, model);
      case ValuesExpression values -> buildValuesPredicate(values, root, query, cb, model);
      case BetweenExpression between -> buildBetweenPredicate(between, root, query, cb, model);
    };
  }

  private static <T> Predicate combinePredicates(
      List<FilterExpression> expressions,
      Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      SearchModel<T> model,
      boolean conjunction) {
    var parts =
        Optional.ofNullable(expressions).orElse(List.of()).stream()
            .filter(Objects::nonNull)
            .map(expression -> toPredicate(expression, root, query, cb, model))
            .toArray(Predicate[]::new);

    if (parts.length == 0) {
      return cb.conjunction();
    }

    return conjunction ? cb.and(parts) : cb.or(parts);
  }

  private static <T> Predicate buildValuePredicate(
      ValueExpression v,
      Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      SearchModel<T> model) {
    ResolvedField resolved =
        model.resolve(root, query, cb, FieldPathNormalizer.normalize(v.field()));
    validateOperator(v.operator(), resolved);

    Expression<?> expr = resolved.expression();
    Object raw = coerceValue(v.value(), resolved.valueType());

    Predicate p =
        switch (v.operator()) {
          case FilterOperators.EQUAL -> cb.equal(expr, raw);

          case FilterOperators.GREATER ->
              cb.greaterThan(asComparableExpression(expr), asComparable(raw));
          case FilterOperators.GREATER_OR_EQUAL ->
              cb.greaterThanOrEqualTo(asComparableExpression(expr), asComparable(raw));
          case FilterOperators.LESS ->
              cb.lessThan(asComparableExpression(expr), asComparable(raw));
          case FilterOperators.LESS_OR_EQUAL ->
              cb.lessThanOrEqualTo(asComparableExpression(expr), asComparable(raw));

          case FilterOperators.LIKE ->
              buildLike(cb, expr.as(String.class), Objects.toString(raw, ""), v.caseSensitive());

          default ->
              throw new IllegalArgumentException("Unsupported value operator: " + v.operator());
        };

    return maybeNegate(v.exclude(), p, cb);
  }

  private static <T> Predicate buildValuesPredicate(
      ValuesExpression v,
      Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      SearchModel<T> model) {
    if (!FilterOperators.IN.equals(v.operator())) {
      throw new IllegalArgumentException("Unsupported values operator: " + v.operator());
    }

    ResolvedField resolved =
        model.resolve(root, query, cb, FieldPathNormalizer.normalize(v.field()));
    validateOperator(v.operator(), resolved);

    CriteriaBuilder.In<Object> in = cb.in(resolved.expression());
    for (Object o : Optional.ofNullable(v.values()).orElse(List.of())) {
      in.value(coerceValue(o, resolved.valueType()));
    }

    return maybeNegate(v.exclude(), in, cb);
  }

  private static <T> Predicate buildBetweenPredicate(
      BetweenExpression b,
      Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      SearchModel<T> model) {
    if (!FilterOperators.BETWEEN.equals(b.operator())) {
      throw new IllegalArgumentException("Unsupported between operator: " + b.operator());
    }

    ResolvedField resolved =
        model.resolve(root, query, cb, FieldPathNormalizer.normalize(b.field()));
    validateOperator(b.operator(), resolved);

    @SuppressWarnings({"rawtypes", "unchecked"})
    Predicate p =
        cb.between(
            (Expression) asComparableExpression(resolved.expression()),
            (Comparable) asComparable(coerceValue(b.from(), resolved.valueType())),
            (Comparable) asComparable(coerceValue(b.to(), resolved.valueType())));

    return maybeNegate(b.exclude(), p, cb);
  }

  private static void validateOperator(String operator, ResolvedField resolved) {
    if (!resolved.supportedOperators().contains(operator)) {
      throw new IllegalArgumentException(
          "Operator '" + operator + "' is not allowed for this field");
    }
  }

  private static Predicate maybeNegate(boolean exclude, Predicate predicate, CriteriaBuilder cb) {
    return exclude ? cb.not(predicate) : predicate;
  }

  private static Predicate buildLike(
      CriteriaBuilder cb, Expression<String> expr, String value, Boolean caseSensitive) {
    boolean cs = Boolean.TRUE.equals(caseSensitive);
    if (cs) {
      return cb.like(expr, value);
    }
    return cb.like(cb.lower(expr), value.toLowerCase(Locale.ROOT));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static <Y extends Comparable> Expression<Y> asComparableExpression(Expression<?> expr) {
    return (Expression) expr;
  }

  @SuppressWarnings("rawtypes")
  private static Comparable asComparable(Object raw) {
    if (raw == null) {
      return null;
    }
    if (raw instanceof Comparable c) {
      return c;
    }
    throw new IllegalArgumentException("Value is not comparable: " + raw);
  }

  private static Object coerceValue(Object raw, SearchValueType valueType) {
    if (raw == null) {
      return null;
    }

    return switch (valueType) {
      case STRING -> Objects.toString(raw, null);
      case UUID -> raw instanceof UUID uuid ? uuid : UUID.fromString(Objects.toString(raw));
      case NUMBER -> coerceNumber(raw);
      case BOOLEAN -> raw instanceof Boolean bool ? bool : Boolean.valueOf(Objects.toString(raw));
      case DATE -> raw instanceof LocalDate date ? date : LocalDate.parse(Objects.toString(raw));
      case DATETIME ->
          raw instanceof OffsetDateTime dateTime
              ? dateTime
              : OffsetDateTime.parse(Objects.toString(raw));
      case UNKNOWN -> raw;
    };
  }

  private static BigDecimal coerceNumber(Object raw) {
    return switch (raw) {
      case BigDecimal decimal -> decimal;
      case Number number -> new BigDecimal(number.toString());
      default -> new BigDecimal(Objects.toString(raw));
    };
  }
}
