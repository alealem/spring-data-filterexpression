package com.example.demo.services.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public final class FieldPathDtoDeserializer extends StdDeserializer<FieldPathDto> {

  public FieldPathDtoDeserializer() {
    super(FieldPathDto.class);
  }

  @Override
  public FieldPathDto deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JacksonException {

    JsonNode node = p.readValueAsTree();

    if (node == null || node.isNull()) {
      return new FieldPathDto(List.of());
    }

    if (node.isTextual()) {
      String text = node.asText();
      if (text.isBlank()) {
        return new FieldPathDto(List.of());
      }
      return new FieldPathDto(List.of(text.split("\\.")));
    }

    if (node.isArray()) {
      List<String> segments = new ArrayList<>();
      for (JsonNode element : node) {
        if (element != null && !element.isNull()) {
          segments.add(element.asText());
        }
      }
      return new FieldPathDto(segments);
    }

    throw JsonMappingException.from(p, "Unsupported JSON value for FieldPathDto: " + node);
  }
}
