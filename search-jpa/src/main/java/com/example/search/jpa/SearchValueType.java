package com.example.search.jpa;

import java.util.Set;

public enum SearchValueType {
  STRING,
  NUMBER,
  BOOLEAN,
  UUID,
  DATE,
  DATETIME,
  UNKNOWN;

  public Set<String> defaultOperators() {
    return switch (this) {
      case STRING, UUID -> FilterOperators.STRING_OPERATORS;
      case NUMBER, DATE, DATETIME -> FilterOperators.COMPARABLE_OPERATORS;
      case BOOLEAN -> Set.of(FilterOperators.EQUAL, FilterOperators.IN);
      case UNKNOWN -> throw new IllegalStateException("UNKNOWN has no default operators");
    };
  }
}
