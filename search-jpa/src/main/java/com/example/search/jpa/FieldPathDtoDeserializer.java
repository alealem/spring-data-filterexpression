package com.example.search.jpa;

import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;

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
      return new FieldPathDto(FieldPathNormalizer.expandSegments(List.of(text)));
    }

    if (node.isArray()) {
      return new FieldPathDto(
          FieldPathNormalizer.expandSegments(
              StreamSupport.stream(node.spliterator(), false).map(JsonNode::asText).toList()));
    }

    throw JsonMappingException.from(p, "Unsupported JSON value for FieldPathDto: " + node);
  }
}
