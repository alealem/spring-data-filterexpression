package com.example.demo.services.search;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;

public final class SearchModels {

  private SearchModels() {}

  public static final Set<String> STRING_OPERATORS = Set.of("equal", "like", "in");

  public static final Set<String> COMPARABLE_OPERATORS =
      Set.of("equal", "greater", "greater-or-equal", "less", "less-or-equal", "between", "in");

  public static <T> SearchFieldRoot<T> directField(
      String fieldName, SearchValueType type, Set<String> supportedOperators) {
    return new SearchFieldRoot<>(
        fieldName,
        type,
        supportedOperators,
        (root, query, cb, field, consumedSegments) -> root.get(fieldName));
  }

  public static <T> SearchFieldRoot<T> jsonRootField(
      String rootPrefix, String jsonColumn, SearchValueType type, Set<String> supportedOperators) {
    return new SearchFieldRoot<>(
        rootPrefix,
        type,
        supportedOperators,
        (root, query, cb, field, consumedSegments) -> {
          List<String> segs = field.segments();
          if (segs.size() <= consumedSegments) {
            throw new IllegalArgumentException(
                "JSON path must include at least one nested key after '" + rootPrefix + "'");
          }

          List<Expression<?>> args = new ArrayList<>();
          args.add(root.get(jsonColumn));

          for (int i = consumedSegments; i < segs.size(); i++) {
            args.add(cb.literal(segs.get(i)));
          }

          return cb.function(
              "jsonb_extract_path_text", String.class, args.toArray(new Expression[0]));
        });
  }

  public static <T> SearchFieldRoot<T> joinedJsonRootField(
      String rootPrefix,
      String joinName,
      String jsonColumn,
      SearchValueType type,
      Set<String> supportedOperators) {
    return new SearchFieldRoot<>(
        rootPrefix,
        type,
        supportedOperators,
        (root, query, cb, field, consumedSegments) -> {
          List<String> segs = field.segments();
          if (segs.size() <= consumedSegments) {
            throw new IllegalArgumentException(
                "JSON path must include at least one nested key after '" + rootPrefix + "'");
          }

          query.distinct(true);
          From<?, ?> join = root.join(joinName, JoinType.LEFT);

          List<Expression<?>> args = new ArrayList<>();
          args.add(join.get(jsonColumn));

          for (int i = consumedSegments; i < segs.size(); i++) {
            args.add(cb.literal(segs.get(i)));
          }

          return cb.function(
              "jsonb_extract_path_text", String.class, args.toArray(new Expression[0]));
        });
  }

  public static <T> SearchFieldRoot<T> numericJsonField(
      String fullPath,
      String jsonColumn,
      List<String> jsonPath,
      SearchValueType type,
      Set<String> supportedOperators) {
    return new SearchFieldRoot<>(
        fullPath,
        type,
        supportedOperators,
        (root, query, cb, field, consumedSegments) -> {
          List<Expression<?>> args = new ArrayList<>();
          args.add(root.get(jsonColumn));
          for (String part : jsonPath) {
            args.add(cb.literal(part));
          }
          return cb.function(
              "jsonb_extract_path_numeric", BigDecimal.class, args.toArray(new Expression[0]));
        });
  }

  public static <T> SearchFieldRoot<T> joinedNumericJsonField(
      String fullPath,
      String joinName,
      String jsonColumn,
      List<String> jsonPath,
      SearchValueType type,
      Set<String> supportedOperators) {
    return new SearchFieldRoot<>(
        fullPath,
        type,
        supportedOperators,
        (root, query, cb, field, consumedSegments) -> {
          query.distinct(true);
          From<?, ?> join = root.join(joinName, JoinType.LEFT);

          List<Expression<?>> args = new ArrayList<>();
          args.add(join.get(jsonColumn));
          for (String part : jsonPath) {
            args.add(cb.literal(part));
          }
          return cb.function(
              "jsonb_extract_path_numeric", BigDecimal.class, args.toArray(new Expression[0]));
        });
  }
}
