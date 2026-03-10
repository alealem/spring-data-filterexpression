package com.example.demo.services.search;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

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
    if (expr == null) {
      return cb.conjunction();
    }

    if (expr instanceof AndExpression and) {
      Predicate[] parts =
          and.expressions().stream()
              .filter(Objects::nonNull)
              .map(e -> toPredicate(e, root, query, cb, model))
              .toArray(Predicate[]::new);

      return parts.length == 0 ? cb.conjunction() : cb.and(parts);
    }

    if (expr instanceof OrExpression or) {
      Predicate[] parts =
          or.expressions().stream()
              .filter(Objects::nonNull)
              .map(e -> toPredicate(e, root, query, cb, model))
              .toArray(Predicate[]::new);

      return parts.length == 0 ? cb.conjunction() : cb.or(parts);
    }

    if (expr instanceof NotExpression not) {
      return cb.not(toPredicate(not.expression(), root, query, cb, model));
    }

    if (expr instanceof ValueExpression v) {
      return buildValuePredicate(v, root, query, cb, model);
    }

    if (expr instanceof ValuesExpression v) {
      return buildValuesPredicate(v, root, query, cb, model);
    }

    if (expr instanceof BetweenExpression b) {
      return buildBetweenPredicate(b, root, query, cb, model);
    }

    throw new IllegalArgumentException("Unsupported operator: " + expr.operator());
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
    Object raw = v.value();

    Predicate p =
        switch (v.operator()) {
          case "equal" -> cb.equal(expr, raw);

          case "greater" -> cb.greaterThan(asComparableExpression(expr), asComparable(raw));
          case "greater-or-equal" ->
              cb.greaterThanOrEqualTo(asComparableExpression(expr), asComparable(raw));
          case "less" -> cb.lessThan(asComparableExpression(expr), asComparable(raw));
          case "less-or-equal" ->
              cb.lessThanOrEqualTo(asComparableExpression(expr), asComparable(raw));

          case "like" ->
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
    if (!"in".equals(v.operator())) {
      throw new IllegalArgumentException("Unsupported values operator: " + v.operator());
    }

    ResolvedField resolved =
        model.resolve(root, query, cb, FieldPathNormalizer.normalize(v.field()));
    validateOperator(v.operator(), resolved);

    CriteriaBuilder.In<Object> in = cb.in(resolved.expression());
    for (Object o : Optional.ofNullable(v.values()).orElse(List.of())) {
      in.value(o);
    }

    return maybeNegate(v.exclude(), in, cb);
  }

  private static <T> Predicate buildBetweenPredicate(
      BetweenExpression b,
      Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      SearchModel<T> model) {
    if (!"between".equals(b.operator())) {
      throw new IllegalArgumentException("Unsupported between operator: " + b.operator());
    }

    ResolvedField resolved =
        model.resolve(root, query, cb, FieldPathNormalizer.normalize(b.field()));
    validateOperator(b.operator(), resolved);

    @SuppressWarnings({"rawtypes", "unchecked"})
    Predicate p =
        cb.between(
            (Expression) asComparableExpression(resolved.expression()),
            (Comparable) asComparable(b.from()),
            (Comparable) asComparable(b.to()));

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
}
