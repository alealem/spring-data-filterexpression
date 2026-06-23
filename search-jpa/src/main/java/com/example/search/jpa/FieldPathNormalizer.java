package com.example.search.jpa;

import java.util.ArrayList;
import java.util.List;

public final class FieldPathNormalizer {

  private FieldPathNormalizer() {}

  public static FieldPathDto normalize(Object rawField) {
    return switch (rawField) {
      case null -> new FieldPathDto(List.of());
      case FieldPathDto dto -> dto;
      case String s when s.isBlank() -> new FieldPathDto(List.of());
      case String s -> new FieldPathDto(expandSegments(List.of(s)));
      case List<?> list -> new FieldPathDto(expandSegments(list));
      default ->
          throw new IllegalArgumentException("Unsupported field format: " + rawField.getClass());
    };
  }

  static List<String> expandSegments(Iterable<?> rawSegments) {
    List<String> segments = new ArrayList<>();
    for (Object rawSegment : rawSegments) {
      if (rawSegment == null) {
        continue;
      }

      String text = String.valueOf(rawSegment).trim();
      if (text.isEmpty()) {
        continue;
      }

      for (String part : text.split("\\.")) {
        if (!part.isBlank()) {
          segments.add(part);
        }
      }
    }
    return List.copyOf(segments);
  }
}
