package com.example.demo.services.search.models;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.domain.Content;
import com.example.search.jpa.FilterOperators;
import com.example.search.jpa.SearchModel;
import com.example.search.jpa.SearchValueType;

@Component
public class ContentSearchModel extends SearchModel<Content> {

  public ContentSearchModel() {
    super(
        SearchModel.<Content>builder()
            .directField("id", SearchValueType.UUID, FilterOperators.STRING_OPERATORS)
            .directField("title", SearchValueType.STRING, FilterOperators.STRING_OPERATORS)
            .directField(
                "description", SearchValueType.STRING, FilterOperators.STRING_OPERATORS)
            .joinedNumericJsonField(
                "items.body.amount.total",
                "items",
                "body",
                List.of("amount", "total"),
                SearchValueType.NUMBER,
                FilterOperators.COMPARABLE_OPERATORS)
            .joinedNumericJsonField(
                "items.body.amount.subtotal",
                "items",
                "body",
                List.of("amount", "subtotal"),
                SearchValueType.NUMBER,
                FilterOperators.COMPARABLE_OPERATORS)
            .joinedNumericJsonField(
                "items.body.amount.tax",
                "items",
                "body",
                List.of("amount", "tax"),
                SearchValueType.NUMBER,
                FilterOperators.COMPARABLE_OPERATORS)
            .joinedJsonField(
                "items.body", "items", "body", SearchValueType.STRING, FilterOperators.STRING_OPERATORS));
  }
}
