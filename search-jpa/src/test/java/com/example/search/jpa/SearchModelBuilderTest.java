package com.example.search.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class SearchModelBuilderTest {

  @Test
  void shouldPreferMostSpecificPrefix() {
    AtomicInteger consumedSegments = new AtomicInteger(-1);

    SearchModel<Object> model =
        SearchModel.<Object>builder()
            .field(
                "items.body",
                SearchValueType.STRING,
                FilterOperators.STRING_OPERATORS,
                (root, query, cb, field, consumed) -> {
                  consumedSegments.set(consumed);
                  return null;
                })
            .field(
                "items",
                SearchValueType.STRING,
                FilterOperators.STRING_OPERATORS,
                (root, query, cb, field, consumed) -> {
                  throw new AssertionError("Less specific prefix should not be chosen");
                })
            .build();

    ResolvedField resolved =
        model.resolve(null, null, null, FieldPathNormalizer.normalize("items.body.type"));

    assertEquals(null, resolved.expression());
    assertEquals(2, consumedSegments.get());
  }
}
