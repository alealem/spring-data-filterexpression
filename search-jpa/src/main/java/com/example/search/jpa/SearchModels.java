package com.example.search.jpa;

import java.math.BigDecimal;
import java.util.Set;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;

final class SearchModels {

  private SearchModels() {}

  static <T> SearchFieldRoot<T> propertyField(
      String fieldPath, SearchValueType type, Set<String> supportedOperators) {
    return new SearchFieldRoot<>(
        fieldPath,
        type,
        supportedOperators,
        (root, query, cb, field, consumedSegments) -> resolvePropertyPath(root, fieldPath));
  }

  public static <T> SearchFieldRoot<T> directField(
      String fieldName, SearchValueType type, Set<String> supportedOperators) {
    return propertyField(fieldName, type, supportedOperators);
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
          args.add(resolvePropertyPath(root, jsonColumn));

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
          args.add(resolvePropertyPath(join, jsonColumn));

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
          args.add(resolvePropertyPath(root, jsonColumn));
          for (String part : jsonPath) {
            args.add(cb.literal(part));
          }
          return cb.function(
              "jsonb_extract_path_numeric", BigDecimal.class, args.toArray(new Expression[0]));
        });
  }

  public static <T> SearchFieldRoot<T> dateJsonField(
      String fullPath,
      String jsonColumn,
      List<String> jsonPath,
      SearchValueType type,
      Set<String> supportedOperators) {
    return typedJsonField(
        fullPath,
        type,
        supportedOperators,
        LocalDate.class,
        "jsonb_extract_path_date",
        jsonColumn,
        jsonPath);
  }

  public static <T> SearchFieldRoot<T> dateTimeJsonField(
      String fullPath,
      String jsonColumn,
      List<String> jsonPath,
      SearchValueType type,
      Set<String> supportedOperators) {
    return typedJsonField(
        fullPath,
        type,
        supportedOperators,
        OffsetDateTime.class,
        "jsonb_extract_path_tstz",
        jsonColumn,
        jsonPath);
  }

  public static <T> SearchFieldRoot<T> boolJsonField(
      String fullPath,
      String jsonColumn,
      List<String> jsonPath,
      SearchValueType type,
      Set<String> supportedOperators) {
    return typedJsonField(
        fullPath,
        type,
        supportedOperators,
        Boolean.class,
        "jsonb_extract_path_bool",
        jsonColumn,
        jsonPath);
  }

  public static <T> SearchFieldRoot<T> uuidJsonField(
      String fullPath,
      String jsonColumn,
      List<String> jsonPath,
      SearchValueType type,
      Set<String> supportedOperators) {
    return typedJsonField(
        fullPath,
        type,
        supportedOperators,
        UUID.class,
        "jsonb_extract_path_uuid",
        jsonColumn,
        jsonPath);
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
          args.add(resolvePropertyPath(join, jsonColumn));
          for (String part : jsonPath) {
            args.add(cb.literal(part));
          }
          return cb.function(
              "jsonb_extract_path_numeric", BigDecimal.class, args.toArray(new Expression[0]));
        });
  }

  public static <T> SearchFieldRoot<T> joinedDateJsonField(
      String fullPath,
      String joinName,
      String jsonColumn,
      List<String> jsonPath,
      SearchValueType type,
      Set<String> supportedOperators) {
    return joinedTypedJsonField(
        fullPath,
        type,
        supportedOperators,
        LocalDate.class,
        "jsonb_extract_path_date",
        joinName,
        jsonColumn,
        jsonPath);
  }

  public static <T> SearchFieldRoot<T> joinedDateTimeJsonField(
      String fullPath,
      String joinName,
      String jsonColumn,
      List<String> jsonPath,
      SearchValueType type,
      Set<String> supportedOperators) {
    return joinedTypedJsonField(
        fullPath,
        type,
        supportedOperators,
        OffsetDateTime.class,
        "jsonb_extract_path_tstz",
        joinName,
        jsonColumn,
        jsonPath);
  }

  public static <T> SearchFieldRoot<T> joinedBoolJsonField(
      String fullPath,
      String joinName,
      String jsonColumn,
      List<String> jsonPath,
      SearchValueType type,
      Set<String> supportedOperators) {
    return joinedTypedJsonField(
        fullPath,
        type,
        supportedOperators,
        Boolean.class,
        "jsonb_extract_path_bool",
        joinName,
        jsonColumn,
        jsonPath);
  }

  public static <T> SearchFieldRoot<T> joinedUuidJsonField(
      String fullPath,
      String joinName,
      String jsonColumn,
      List<String> jsonPath,
      SearchValueType type,
      Set<String> supportedOperators) {
    return joinedTypedJsonField(
        fullPath,
        type,
        supportedOperators,
        UUID.class,
        "jsonb_extract_path_uuid",
        joinName,
        jsonColumn,
        jsonPath);
  }

  private static <T, Y> SearchFieldRoot<T> typedJsonField(
      String fullPath,
      SearchValueType type,
      Set<String> supportedOperators,
      Class<Y> resultType,
      String functionName,
      String jsonColumn,
      List<String> jsonPath) {
    return new SearchFieldRoot<>(
        fullPath,
        type,
        supportedOperators,
        (root, query, cb, field, consumedSegments) -> {
          List<Expression<?>> args = new ArrayList<>();
          args.add(resolvePropertyPath(root, jsonColumn));
          for (String part : jsonPath) {
            args.add(cb.literal(part));
          }
          return cb.function(functionName, resultType, args.toArray(new Expression[0]));
        });
  }

  private static <T, Y> SearchFieldRoot<T> joinedTypedJsonField(
      String fullPath,
      SearchValueType type,
      Set<String> supportedOperators,
      Class<Y> resultType,
      String functionName,
      String joinName,
      String jsonColumn,
      List<String> jsonPath) {
    return new SearchFieldRoot<>(
        fullPath,
        type,
        supportedOperators,
        (root, query, cb, field, consumedSegments) -> {
          query.distinct(true);
          From<?, ?> join = root.join(joinName, JoinType.LEFT);

          List<Expression<?>> args = new ArrayList<>();
          args.add(resolvePropertyPath(join, jsonColumn));
          for (String part : jsonPath) {
            args.add(cb.literal(part));
          }
          return cb.function(functionName, resultType, args.toArray(new Expression[0]));
        });
  }

  private static Path<?> resolvePropertyPath(Path<?> root, String fieldPath) {
    Path<?> current = root;
    for (String segment : FieldPathNormalizer.expandSegments(List.of(fieldPath))) {
      current = current.get(segment);
    }
    return current;
  }
}
