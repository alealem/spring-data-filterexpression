package com.example.demo.services.search;

import java.util.Set;

public final class SearchFieldRoot<T> {

  private final String pathPrefix;
  private final SearchValueType valueType;
  private final Set<String> supportedOperators;
  private final FieldTarget<T> target;

  public SearchFieldRoot(
      String pathPrefix,
      SearchValueType valueType,
      Set<String> supportedOperators,
      FieldTarget<T> target) {
    this.pathPrefix = pathPrefix;
    this.valueType = valueType;
    this.supportedOperators = supportedOperators;
    this.target = target;
  }

  public String pathPrefix() {
    return pathPrefix;
  }

  public SearchValueType valueType() {
    return valueType;
  }

  public Set<String> supportedOperators() {
    return supportedOperators;
  }

  public FieldTarget<T> target() {
    return target;
  }
}
