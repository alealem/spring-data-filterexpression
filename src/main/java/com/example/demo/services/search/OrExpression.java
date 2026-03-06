package com.example.demo.services.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OrExpression(
        @JsonProperty("operator") String operator,
        @JsonProperty("expressions") List<FilterExpression> expressions
) implements FilterExpression {}
