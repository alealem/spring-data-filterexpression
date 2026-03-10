package com.example.demo.services.search;

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

  public static <T> SearchFieldRoot<T> numericJsonRootField(
      String rootPrefix, String jsonColumn, SearchValueType type, Set<String> supportedOperators) {
    return new SearchFieldRoot<>(
        rootPrefix,
        type,
        supportedOperators,
        (root, query, cb, field, consumedSegments) -> {
          List<String> segs = field.segments();

          if (segs.size() < consumedSegments) {
            throw new IllegalArgumentException(
                "JSON numeric path must match or extend '" + rootPrefix + "'");
          }

          List<Expression<?>> args = new ArrayList<>();
          args.add(root.get(jsonColumn));

          for (int i = 1; i < consumedSegments; i++) {
            args.add(cb.literal(segs.get(i)));
          }

          return cb.function(
              "jsonb_extract_path_numeric",
              java.math.BigDecimal.class,
              args.toArray(new Expression[0]));
        });
  }
    public static <T> SearchFieldRoot<T> joinedNumericJsonRootField(
            String rootPrefix,
            String joinName,
            String jsonColumn,
            SearchValueType type,
            Set<String> supportedOperators
    ) {
        return new SearchFieldRoot<>(
                rootPrefix,
                type,
                supportedOperators,
                (root, query, cb, field, consumedSegments) -> {
                    List<String> segs = field.segments();

                    query.distinct(true);

                    var join = root.join(joinName, jakarta.persistence.criteria.JoinType.LEFT);

                    List<jakarta.persistence.criteria.Expression<?>> args = new ArrayList<>();
                    args.add(join.get(jsonColumn));

                    for (int i = 1; i < consumedSegments; i++) {
                        args.add(cb.literal(segs.get(i)));
                    }

                    return cb.function(
                            "jsonb_extract_path_numeric",
                            java.math.BigDecimal.class,
                            args.toArray(new jakarta.persistence.criteria.Expression[0])
                    );
                }
        );
    }
}
