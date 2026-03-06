package com.example.demo.services.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BetweenExpression(
        @JsonProperty("operator") String operator,
        @JsonProperty("exclude") boolean exclude,
        @JsonProperty("field") FieldPathDto field,
        @JsonProperty("from") Object from,
        @JsonProperty("to") Object to
) implements FilterExpression {}