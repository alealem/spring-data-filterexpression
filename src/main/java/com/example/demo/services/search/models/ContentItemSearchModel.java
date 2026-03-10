package com.example.demo.services.search.models;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.domain.ContentItem;
import com.example.demo.services.search.SearchFieldRoot;
import com.example.demo.services.search.SearchModel;
import com.example.demo.services.search.SearchModels;
import com.example.demo.services.search.SearchValueType;

@Component
public class ContentItemSearchModel extends SearchModel<ContentItem> {

  public ContentItemSearchModel() {
    super(
        List.of(
            SearchModels.directField("id", SearchValueType.UUID, SearchModels.STRING_OPERATORS),
            new SearchFieldRoot<>(
                "content.id",
                SearchValueType.UUID,
                SearchModels.STRING_OPERATORS,
                (root, query, cb, field, consumedSegments) -> root.get("content").get("id")),
            SearchModels.numericJsonField(
                "body.amount.total",
                "body",
                List.of("amount", "total"),
                SearchValueType.NUMBER,
                SearchModels.COMPARABLE_OPERATORS),
            SearchModels.numericJsonField(
                "body.amount.subtotal",
                "body",
                List.of("amount", "subtotal"),
                SearchValueType.NUMBER,
                SearchModels.COMPARABLE_OPERATORS),
            SearchModels.numericJsonField(
                "body.amount.tax",
                "body",
                List.of("amount", "tax"),
                SearchValueType.NUMBER,
                SearchModels.COMPARABLE_OPERATORS),
            SearchModels.jsonRootField(
                "body", "body", SearchValueType.STRING, SearchModels.STRING_OPERATORS)));
  }
}
