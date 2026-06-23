package com.example.search.jpa;

import java.util.Set;

public final class FilterOperators {

  public static final String EQUAL = "equal";
  public static final String GREATER = "greater";
  public static final String GREATER_OR_EQUAL = "greater-or-equal";
  public static final String LESS = "less";
  public static final String LESS_OR_EQUAL = "less-or-equal";
  public static final String LIKE = "like";
  public static final String IN = "in";
  public static final String BETWEEN = "between";
  public static final String AND = "and";
  public static final String OR = "or";
  public static final String NOT = "not";

  public static final Set<String> STRING_OPERATORS = Set.of(EQUAL, LIKE, IN);

  public static final Set<String> COMPARABLE_OPERATORS =
      Set.of(EQUAL, GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, BETWEEN, IN);

  private FilterOperators() {}
}
