package com.example.demo.services.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ComboSelectionExpression(
    @JsonProperty("operator") String operator,
    @JsonProperty("field") FieldPathDto field,
    @JsonProperty("value") ComboSelectionValuesDto value)
    implements FilterExpression {}
