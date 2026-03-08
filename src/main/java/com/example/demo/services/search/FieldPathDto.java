package com.example.demo.services.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/** Accepts JSON like: "customer.name" or ["customer","name"] */
@JsonDeserialize(using = FieldPathDtoDeserializer.class)
public final class FieldPathDto {

  private final List<String> segments;

  public FieldPathDto(List<String> segments) {
    this.segments = segments == null ? Collections.emptyList() : List.copyOf(segments);
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static FieldPathDto fromJson(Object value) {
    if (value == null) {
      return new FieldPathDto(List.of());
    }

    if (value instanceof String s) {
      if (s.isBlank()) {
        return new FieldPathDto(List.of());
      }
      return new FieldPathDto(List.of(s.split("\\.")));
    }

    if (value instanceof List<?> list) {
      List<String> segs = new ArrayList<>(list.size());
      for (Object o : list) {
        if (o != null) {
          segs.add(String.valueOf(o));
        }
      }
      return new FieldPathDto(segs);
    }

    throw new IllegalArgumentException("Unsupported field format: " + value.getClass());
  }

  public List<String> segments() {
    return segments;
  }

  @Override
  public String toString() {
    return "FieldPathDto{" + "segments=" + segments + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FieldPathDto that)) return false;
    return Objects.equals(segments, that.segments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(segments);
  }
}
