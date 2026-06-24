package com.example.search.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SearchModelBuilder<T> {

  private final List<SearchFieldRoot<T>> roots = new ArrayList<>();

  SearchModelBuilder() {}

  public SearchModelBuilder<T> field(SearchFieldRoot<T> root) {
    boolean duplicate =
        roots.stream().anyMatch(existing -> existing.pathPrefix().equals(root.pathPrefix()));
    if (duplicate) {
      throw new IllegalArgumentException("Duplicate search field: " + root.pathPrefix());
    }
    roots.add(root);
    return this;
  }

  public SearchModelBuilder<T> autoDiscoverSearchable(Class<T> rootType) {
    SearchableFieldDiscovery.discover(rootType).forEach(this::field);
    return this;
  }

  public SearchModelBuilder<T> field(
      String pathPrefix,
      SearchValueType valueType,
      Set<String> supportedOperators,
      FieldTarget<T> target) {
    return field(new SearchFieldRoot<>(pathPrefix, valueType, supportedOperators, target));
  }

  public SearchModelBuilder<T> directField(
      String fieldName, SearchValueType valueType, Set<String> supportedOperators) {
    return field(SearchModels.directField(fieldName, valueType, supportedOperators));
  }

  public SearchModelBuilder<T> pathField(
      String fieldPath, SearchValueType valueType, Set<String> supportedOperators) {
    return field(SearchModels.propertyField(fieldPath, valueType, supportedOperators));
  }

  public SearchModelBuilder<T> stringField(String fieldPath) {
    return pathField(fieldPath, SearchValueType.STRING, FilterOperators.STRING_OPERATORS);
  }

  public SearchModelBuilder<T> uuidField(String fieldPath) {
    return pathField(fieldPath, SearchValueType.UUID, FilterOperators.STRING_OPERATORS);
  }

  public SearchModelBuilder<T> numberField(String fieldPath) {
    return pathField(fieldPath, SearchValueType.NUMBER, FilterOperators.COMPARABLE_OPERATORS);
  }

  public JsonFieldScope json(String rootPrefix) {
    return new JsonFieldScope(rootPrefix, rootPrefix, null);
  }

  public JsonFieldScope joinedJson(String rootPrefix) {
    JoinedJsonPath path = JoinedJsonPath.parse(rootPrefix);
    return new JsonFieldScope(path.rootPrefix(), path.jsonColumn(), path.joinName());
  }

  public SearchModelBuilder<T> jsonField(
      String rootPrefix,
      String jsonColumn,
      SearchValueType valueType,
      Set<String> supportedOperators) {
    return field(SearchModels.jsonRootField(rootPrefix, jsonColumn, valueType, supportedOperators));
  }

  public SearchModelBuilder<T> joinedJsonField(
      String rootPrefix,
      String joinName,
      String jsonColumn,
      SearchValueType valueType,
      Set<String> supportedOperators) {
    return field(
        SearchModels.joinedJsonRootField(
            rootPrefix, joinName, jsonColumn, valueType, supportedOperators));
  }

  public SearchModelBuilder<T> jsonStringField(String rootPrefix) {
    return jsonField(
        rootPrefix, rootPrefix, SearchValueType.STRING, FilterOperators.STRING_OPERATORS);
  }

  public SearchModelBuilder<T> joinedJsonStringField(String rootPrefix) {
    List<String> segments = FieldPathNormalizer.expandSegments(List.of(rootPrefix));
    if (segments.size() != 2) {
      throw new IllegalArgumentException(
          "joinedJsonStringField expects '<join>.<jsonColumn>' but got: " + rootPrefix);
    }

    return joinedJsonField(
        rootPrefix,
        segments.getFirst(),
        segments.getLast(),
        SearchValueType.STRING,
        FilterOperators.STRING_OPERATORS);
  }

  public SearchModelBuilder<T> numericJsonField(
      String fullPath,
      String jsonColumn,
      List<String> jsonPath,
      SearchValueType valueType,
      Set<String> supportedOperators) {
    return field(
        SearchModels.numericJsonField(
            fullPath, jsonColumn, jsonPath, valueType, supportedOperators));
  }

  public SearchModelBuilder<T> numberJsonField(String fullPath) {
    List<String> segments = FieldPathNormalizer.expandSegments(List.of(fullPath));
    if (segments.size() < 2) {
      throw new IllegalArgumentException(
          "numberJsonField expects '<jsonColumn>.<key>' but got: " + fullPath);
    }

    return numericJsonField(
        fullPath,
        segments.getFirst(),
        segments.subList(1, segments.size()),
        SearchValueType.NUMBER,
        FilterOperators.COMPARABLE_OPERATORS);
  }

  public SearchModelBuilder<T> dateJsonField(String fullPath) {
    List<String> segments = FieldPathNormalizer.expandSegments(List.of(fullPath));
    if (segments.size() < 2) {
      throw new IllegalArgumentException(
          "dateJsonField expects '<jsonColumn>.<key>' but got: " + fullPath);
    }

    return field(
        SearchModels.dateJsonField(
            fullPath,
            segments.getFirst(),
            segments.subList(1, segments.size()),
            SearchValueType.DATE,
            FilterOperators.COMPARABLE_OPERATORS));
  }

  public SearchModelBuilder<T> dateTimeJsonField(String fullPath) {
    List<String> segments = FieldPathNormalizer.expandSegments(List.of(fullPath));
    if (segments.size() < 2) {
      throw new IllegalArgumentException(
          "dateTimeJsonField expects '<jsonColumn>.<key>' but got: " + fullPath);
    }

    return field(
        SearchModels.dateTimeJsonField(
            fullPath,
            segments.getFirst(),
            segments.subList(1, segments.size()),
            SearchValueType.DATETIME,
            FilterOperators.COMPARABLE_OPERATORS));
  }

  public SearchModelBuilder<T> booleanJsonField(String fullPath) {
    List<String> segments = FieldPathNormalizer.expandSegments(List.of(fullPath));
    if (segments.size() < 2) {
      throw new IllegalArgumentException(
          "booleanJsonField expects '<jsonColumn>.<key>' but got: " + fullPath);
    }

    return field(
        SearchModels.boolJsonField(
            fullPath,
            segments.getFirst(),
            segments.subList(1, segments.size()),
            SearchValueType.BOOLEAN,
            Set.of(FilterOperators.EQUAL, FilterOperators.IN)));
  }

  public SearchModelBuilder<T> uuidJsonField(String fullPath) {
    List<String> segments = FieldPathNormalizer.expandSegments(List.of(fullPath));
    if (segments.size() < 2) {
      throw new IllegalArgumentException(
          "uuidJsonField expects '<jsonColumn>.<key>' but got: " + fullPath);
    }

    return field(
        SearchModels.uuidJsonField(
            fullPath,
            segments.getFirst(),
            segments.subList(1, segments.size()),
            SearchValueType.UUID,
            FilterOperators.STRING_OPERATORS));
  }

  public SearchModelBuilder<T> joinedNumericJsonField(
      String fullPath,
      String joinName,
      String jsonColumn,
      List<String> jsonPath,
      SearchValueType valueType,
      Set<String> supportedOperators) {
    return field(
        SearchModels.joinedNumericJsonField(
            fullPath, joinName, jsonColumn, jsonPath, valueType, supportedOperators));
  }

  public SearchModelBuilder<T> joinedNumberJsonField(String fullPath) {
    List<String> segments = FieldPathNormalizer.expandSegments(List.of(fullPath));
    if (segments.size() < 3) {
      throw new IllegalArgumentException(
          "joinedNumberJsonField expects '<join>.<jsonColumn>.<key>' but got: " + fullPath);
    }

    return joinedNumericJsonField(
        fullPath,
        segments.getFirst(),
        segments.get(1),
        segments.subList(2, segments.size()),
        SearchValueType.NUMBER,
        FilterOperators.COMPARABLE_OPERATORS);
  }

  public SearchModelBuilder<T> joinedDateJsonField(String fullPath) {
    List<String> segments = FieldPathNormalizer.expandSegments(List.of(fullPath));
    if (segments.size() < 3) {
      throw new IllegalArgumentException(
          "joinedDateJsonField expects '<join>.<jsonColumn>.<key>' but got: " + fullPath);
    }

    return field(
        SearchModels.joinedDateJsonField(
            fullPath,
            segments.getFirst(),
            segments.get(1),
            segments.subList(2, segments.size()),
            SearchValueType.DATE,
            FilterOperators.COMPARABLE_OPERATORS));
  }

  public SearchModelBuilder<T> joinedDateTimeJsonField(String fullPath) {
    List<String> segments = FieldPathNormalizer.expandSegments(List.of(fullPath));
    if (segments.size() < 3) {
      throw new IllegalArgumentException(
          "joinedDateTimeJsonField expects '<join>.<jsonColumn>.<key>' but got: " + fullPath);
    }

    return field(
        SearchModels.joinedDateTimeJsonField(
            fullPath,
            segments.getFirst(),
            segments.get(1),
            segments.subList(2, segments.size()),
            SearchValueType.DATETIME,
            FilterOperators.COMPARABLE_OPERATORS));
  }

  public SearchModelBuilder<T> joinedBooleanJsonField(String fullPath) {
    List<String> segments = FieldPathNormalizer.expandSegments(List.of(fullPath));
    if (segments.size() < 3) {
      throw new IllegalArgumentException(
          "joinedBooleanJsonField expects '<join>.<jsonColumn>.<key>' but got: " + fullPath);
    }

    return field(
        SearchModels.joinedBoolJsonField(
            fullPath,
            segments.getFirst(),
            segments.get(1),
            segments.subList(2, segments.size()),
            SearchValueType.BOOLEAN,
            Set.of(FilterOperators.EQUAL, FilterOperators.IN)));
  }

  public SearchModelBuilder<T> joinedUuidJsonField(String fullPath) {
    List<String> segments = FieldPathNormalizer.expandSegments(List.of(fullPath));
    if (segments.size() < 3) {
      throw new IllegalArgumentException(
          "joinedUuidJsonField expects '<join>.<jsonColumn>.<key>' but got: " + fullPath);
    }

    return field(
        SearchModels.joinedUuidJsonField(
            fullPath,
            segments.getFirst(),
            segments.get(1),
            segments.subList(2, segments.size()),
            SearchValueType.UUID,
            FilterOperators.STRING_OPERATORS));
  }

  public SearchModel<T> build() {
    return new SearchModel<>(roots);
  }

  List<SearchFieldRoot<T>> buildRoots() {
    return List.copyOf(roots);
  }

  public final class JsonFieldScope {

    private final String rootPrefix;
    private final String jsonColumn;
    private final String joinName;

    private JsonFieldScope(String rootPrefix, String jsonColumn, String joinName) {
      this.rootPrefix = rootPrefix;
      this.jsonColumn = jsonColumn;
      this.joinName = joinName;
    }

    public JsonFieldScope number(String relativePath) {
      String fullPath = joinPath(rootPrefix, relativePath);
      if (joinName == null) {
        numberJsonField(fullPath);
      } else {
        joinedNumberJsonField(fullPath);
      }
      return this;
    }

    public JsonFieldScope date(String relativePath) {
      String fullPath = joinPath(rootPrefix, relativePath);
      if (joinName == null) {
        dateJsonField(fullPath);
      } else {
        joinedDateJsonField(fullPath);
      }
      return this;
    }

    public JsonFieldScope dateTime(String relativePath) {
      String fullPath = joinPath(rootPrefix, relativePath);
      if (joinName == null) {
        dateTimeJsonField(fullPath);
      } else {
        joinedDateTimeJsonField(fullPath);
      }
      return this;
    }

    public JsonFieldScope bool(String relativePath) {
      String fullPath = joinPath(rootPrefix, relativePath);
      if (joinName == null) {
        booleanJsonField(fullPath);
      } else {
        joinedBooleanJsonField(fullPath);
      }
      return this;
    }

    public JsonFieldScope uuid(String relativePath) {
      String fullPath = joinPath(rootPrefix, relativePath);
      if (joinName == null) {
        uuidJsonField(fullPath);
      } else {
        joinedUuidJsonField(fullPath);
      }
      return this;
    }

    public SearchModelBuilder<T> stringTree() {
      return joinName == null
          ? jsonField(
              rootPrefix, jsonColumn, SearchValueType.STRING, FilterOperators.STRING_OPERATORS)
          : joinedJsonField(
              rootPrefix,
              joinName,
              jsonColumn,
              SearchValueType.STRING,
              FilterOperators.STRING_OPERATORS);
    }

  }

  private static String joinPath(String rootPrefix, String relativePath) {
    List<String> segments = FieldPathNormalizer.expandSegments(List.of(rootPrefix, relativePath));
    if (segments.size() <= FieldPathNormalizer.expandSegments(List.of(rootPrefix)).size()) {
      throw new IllegalArgumentException("JSON relative path is required");
    }
    return String.join(".", segments);
  }

  private record JoinedJsonPath(String rootPrefix, String joinName, String jsonColumn) {

    private static JoinedJsonPath parse(String rootPrefix) {
      List<String> segments = FieldPathNormalizer.expandSegments(List.of(rootPrefix));
      if (segments.size() < 2) {
        throw new IllegalArgumentException(
            "joinedJson expects '<join>.<jsonColumn>' but got: " + rootPrefix);
      }

      return new JoinedJsonPath(
          rootPrefix, segments.getFirst(), String.join(".", segments.subList(1, segments.size())));
    }
  }
}
