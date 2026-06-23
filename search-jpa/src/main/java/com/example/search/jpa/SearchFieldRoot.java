package com.example.search.jpa;

import java.util.Set;

public record SearchFieldRoot<T>(
    String pathPrefix,
    SearchValueType valueType,
    Set<String> supportedOperators,
    FieldTarget<T> target) {

  public SearchFieldRoot {
    supportedOperators = Set.copyOf(supportedOperators);
  }
}
