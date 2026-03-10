package com.example.demo.services.search;

import java.util.ArrayList;
import java.util.List;

public final class FieldPathNormalizer {

  private FieldPathNormalizer() {}

  public static FieldPathDto normalize(Object rawField) {
    if (rawField == null) {
      return new FieldPathDto(List.of());
    }

    if (rawField instanceof FieldPathDto dto) {
      return dto;
    }

    if (rawField instanceof String s) {
      if (s.isBlank()) {
        return new FieldPathDto(List.of());
      }
      return new FieldPathDto(List.of(s.split("\\.")));
    }

    if (rawField instanceof List<?> list) {
      List<String> segments = new ArrayList<>(list.size());
      for (Object item : list) {
        if (item != null) {
          segments.add(String.valueOf(item));
        }
      }
      return new FieldPathDto(segments);
    }

    throw new IllegalArgumentException("Unsupported field format: " + rawField.getClass());
  }
}
