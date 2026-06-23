package com.example.search.jpa;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NotExpression(
    @JsonProperty("operator") String operator,
    @JsonProperty("expression") FilterExpression expression)
    implements FilterExpression {}
