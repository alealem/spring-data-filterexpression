package com.example.search.jpa;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = FieldPathDtoDeserializer.class)
public record FieldPathDto(List<String> segments) {

  public FieldPathDto {
    segments = segments == null ? List.of() : List.copyOf(segments);
  }
}
