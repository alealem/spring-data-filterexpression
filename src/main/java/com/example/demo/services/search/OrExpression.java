package com.example.demo.services.search;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrExpression(
    @JsonProperty("operator") String operator,
    @JsonProperty("expressions") List<FilterExpression> expressions)
    implements FilterExpression {}
