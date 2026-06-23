package com.example.search.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class FieldPathDtoDeserializerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldDeserializeDottedString() throws Exception {
    FieldCarrier carrier =
        objectMapper.readValue("{\"field\":\"items.body.type\"}", FieldCarrier.class);

    assertEquals(List.of("items", "body", "type"), carrier.field().segments());
  }

  @Test
  void shouldDeserializeSingleDottedStringInsideArray() throws Exception {
    FieldCarrier carrier =
        objectMapper.readValue("{\"field\":[\"items.body.type\"]}", FieldCarrier.class);

    assertEquals(List.of("items", "body", "type"), carrier.field().segments());
  }

  private record FieldCarrier(FieldPathDto field) {}
}
