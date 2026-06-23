package com.example.demo.services.search.models;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.domain.ContentItem;
import com.example.search.jpa.FilterOperators;
import com.example.search.jpa.SearchModel;
import com.example.search.jpa.SearchValueType;

@Component
public class ContentItemSearchModel extends SearchModel<ContentItem> {

  public ContentItemSearchModel() {
    super(
        SearchModel.<ContentItem>builder()
            .directField("id", SearchValueType.UUID, FilterOperators.STRING_OPERATORS)
            .field(
                "content.id",
                SearchValueType.UUID,
                FilterOperators.STRING_OPERATORS,
                (root, query, cb, field, consumedSegments) -> root.get("content").get("id"))
            .numericJsonField(
                "body.amount.total",
                "body",
                List.of("amount", "total"),
                SearchValueType.NUMBER,
                FilterOperators.COMPARABLE_OPERATORS)
            .numericJsonField(
                "body.amount.subtotal",
                "body",
                List.of("amount", "subtotal"),
                SearchValueType.NUMBER,
                FilterOperators.COMPARABLE_OPERATORS)
            .numericJsonField(
                "body.amount.tax",
                "body",
                List.of("amount", "tax"),
                SearchValueType.NUMBER,
                FilterOperators.COMPARABLE_OPERATORS)
            .jsonField("body", "body", SearchValueType.STRING, FilterOperators.STRING_OPERATORS));
  }
}
