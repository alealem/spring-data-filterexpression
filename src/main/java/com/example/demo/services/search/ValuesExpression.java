package com.example.demo.services.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ValuesExpression(
        @JsonProperty("operator") String operator,
        @JsonProperty("exclude") boolean exclude,
        @JsonProperty("field") FieldPathDto field,
        @JsonProperty("values") List<Object> values
) implements FilterExpression {}