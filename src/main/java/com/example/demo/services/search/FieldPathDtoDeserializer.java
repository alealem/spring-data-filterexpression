package com.example.demo.services.search;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Deserializes:
 *   "a.b.c"  -> ["a","b","c"]
 *   ["a","b","c"] -> ["a","b","c"]
 */
public final class FieldPathDtoDeserializer extends ValueDeserializer<FieldPathDto> {

    @Override
    public FieldPathDto deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {

        JsonNode node = p.readValueAsTree();

        if (node == null || node.isNull()) {
            return new FieldPathDto(List.of());
        }

        // Case 1: "customer.name"
        if (node.isTextual()) {
            String text = node.asText();
            if (text.isBlank()) {
                return new FieldPathDto(List.of());
            }
            return new FieldPathDto(List.of(text.split("\\.")));
        }

        // Case 2: ["customer", "name"]
        if (node.isArray()) {
            List<String> segments = new ArrayList<>();
            for (JsonNode element : node) {
                if (element != null && !element.isNull()) {
                    segments.add(element.asText());
                }
            }
            return new FieldPathDto(segments);
        }

        throw DatabindException.from(p,
                "Unsupported JSON value for FieldPathDto: " + node);
    }
}
