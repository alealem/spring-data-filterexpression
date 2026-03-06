package com.example.demo.services.search;

import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Accepts JSON like:
 *   "customer.name"
 * or
 *   ["customer","name"]
 */
@JsonDeserialize(using = FieldPathDtoDeserializer.class)
public final class FieldPathDto {

    private final List<String> segments;

    public FieldPathDto(List<String> segments) {
        this.segments = segments == null
                ? Collections.emptyList()
                : List.copyOf(segments);
    }

    public List<String> segments() {
        return segments;
    }

    @Override
    public String toString() {
        return "FieldPathDto{segments=" + segments + '}';
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
