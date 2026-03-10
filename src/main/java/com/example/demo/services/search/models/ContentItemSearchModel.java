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

  public static final String JSON_COLUMN = "body";

  public ContentItemSearchModel() {
    super(
        List.of(
            SearchModels.directField("id", SearchValueType.UUID, SearchModels.STRING_OPERATORS),
            new SearchFieldRoot<>(
                "content.id",
                SearchValueType.UUID,
                SearchModels.STRING_OPERATORS,
                (root, query, cb, field, consumedSegments) -> root.get("content").get("id")),
            SearchModels.numericJsonRootField(
                "body.amount.total",
                JSON_COLUMN,
                SearchValueType.NUMBER,
                SearchModels.COMPARABLE_OPERATORS),
            SearchModels.numericJsonRootField(
                "body.amount.subtotal",
                JSON_COLUMN,
                SearchValueType.NUMBER,
                SearchModels.COMPARABLE_OPERATORS),
            SearchModels.numericJsonRootField(
                "body.amount.tax",
                JSON_COLUMN,
                SearchValueType.NUMBER,
                SearchModels.COMPARABLE_OPERATORS),
            SearchModels.jsonRootField(
                "body", JSON_COLUMN, SearchValueType.STRING, SearchModels.STRING_OPERATORS)));
  }
}
