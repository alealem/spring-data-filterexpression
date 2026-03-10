package com.example.demo.services.search;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "operator",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ValueExpression.class, name = "equal"),
  @JsonSubTypes.Type(value = ValueExpression.class, name = "greater"),
  @JsonSubTypes.Type(value = ValueExpression.class, name = "greater-or-equal"),
  @JsonSubTypes.Type(value = ValueExpression.class, name = "less"),
  @JsonSubTypes.Type(value = ValueExpression.class, name = "less-or-equal"),
  @JsonSubTypes.Type(value = ValueExpression.class, name = "like"),
  @JsonSubTypes.Type(value = ValuesExpression.class, name = "in"),
  @JsonSubTypes.Type(value = BetweenExpression.class, name = "between"),
  @JsonSubTypes.Type(value = AndExpression.class, name = "and"),
  @JsonSubTypes.Type(value = OrExpression.class, name = "or"),
  @JsonSubTypes.Type(value = NotExpression.class, name = "not")
})
public sealed interface FilterExpression
    permits AndExpression,
        BetweenExpression,
        ComboSelectionExpression,
        NotExpression,
        OrExpression,
        ValueExpression,
        ValuesExpression {

  String operator();
}
