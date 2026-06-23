package com.example.search.jpa;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "operator",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ValueExpression.class, name = FilterOperators.EQUAL),
  @JsonSubTypes.Type(value = ValueExpression.class, name = FilterOperators.GREATER),
  @JsonSubTypes.Type(value = ValueExpression.class, name = FilterOperators.GREATER_OR_EQUAL),
  @JsonSubTypes.Type(value = ValueExpression.class, name = FilterOperators.LESS),
  @JsonSubTypes.Type(value = ValueExpression.class, name = FilterOperators.LESS_OR_EQUAL),
  @JsonSubTypes.Type(value = ValueExpression.class, name = FilterOperators.LIKE),
  @JsonSubTypes.Type(value = ValuesExpression.class, name = FilterOperators.IN),
  @JsonSubTypes.Type(value = BetweenExpression.class, name = FilterOperators.BETWEEN),
  @JsonSubTypes.Type(value = AndExpression.class, name = FilterOperators.AND),
  @JsonSubTypes.Type(value = OrExpression.class, name = FilterOperators.OR),
  @JsonSubTypes.Type(value = NotExpression.class, name = FilterOperators.NOT)
})
public sealed interface FilterExpression
    permits AndExpression,
        BetweenExpression,
        NotExpression,
        OrExpression,
        ValueExpression,
        ValuesExpression {

  String operator();
}
