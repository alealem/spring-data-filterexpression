package com.example.demo.services.search;

import java.util.List;

record ComboSelectionValuesDto(
        List<SingleValueRowDataDto> singleIncludeValues,
        List<SingleValueRowDataDto> singleExcludeValues,
        List<RangeRowDataDto> rangeIncludeValues,
        List<RangeRowDataDto> rangeExcludeValues
) {
}
