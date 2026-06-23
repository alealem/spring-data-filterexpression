package com.example.search.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SearchModelBuilder<T> {

  private final List<SearchFieldRoot<T>> roots = new ArrayList<>();

  SearchModelBuilder() {}

  public SearchModelBuilder<T> field(SearchFieldRoot<T> root) {
    roots.add(root);
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

  public SearchModel<T> build() {
    return new SearchModel<>(roots);
  }

  List<SearchFieldRoot<T>> buildRoots() {
    return List.copyOf(roots);
  }
}
