package com.example.demo.services.search;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ValueExpression(
        @JsonProperty("operator") String operator,
        @JsonProperty("exclude") boolean exclude,
        @JsonProperty("field") FieldPathDto field,
        @JsonProperty("value") Object value,
        @JsonProperty("caseSensitive") Boolean caseSensitive
) implements FilterExpression {}
