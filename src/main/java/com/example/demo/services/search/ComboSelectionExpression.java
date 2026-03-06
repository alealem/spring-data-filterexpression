package com.example.demo.services.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ComboSelectionExpression(
        @JsonProperty("operator") String operator,
        @JsonProperty("field") FieldPathDto field,
        @JsonProperty("value") ComboSelectionValuesDto value
) implements FilterExpression {}

record ComboSelectionValuesDto(
        List<SingleValueRowDataDto> singleIncludeValues,
        List<SingleValueRowDataDto> singleExcludeValues,
        List<RangeRowDataDto> rangeIncludeValues,
        List<RangeRowDataDto> rangeExcludeValues
) {}

record SingleValueRowDataDto(String id, String operator, Object value) {}
record RangeRowDataDto(String id, String operator, Number from, Number to) {}