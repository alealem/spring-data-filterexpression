package com.example.demo.services.search;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ValuesExpression(
    @JsonProperty("operator") String operator,
    @JsonProperty("exclude") boolean exclude,
    @JsonProperty("field") Object field,
    @JsonProperty("values") List<Object> values)
    implements FilterExpression {}
