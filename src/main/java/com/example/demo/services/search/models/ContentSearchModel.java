package com.example.demo.services.search.models;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.domain.Content;
import com.example.demo.services.search.SearchModel;
import com.example.demo.services.search.SearchModels;
import com.example.demo.services.search.SearchValueType;

@Component
public class ContentSearchModel extends SearchModel<Content> {

  public ContentSearchModel() {
    super(
        List.of(
            SearchModels.directField("id", SearchValueType.UUID, SearchModels.STRING_OPERATORS),
            SearchModels.directField(
                "title", SearchValueType.STRING, SearchModels.STRING_OPERATORS),
            SearchModels.directField(
                "description", SearchValueType.STRING, SearchModels.STRING_OPERATORS),
            SearchModels.joinedNumericJsonField(
                "items.body.amount.total",
                "items",
                "body",
                List.of("amount", "total"),
                SearchValueType.NUMBER,
                SearchModels.COMPARABLE_OPERATORS),
            SearchModels.joinedNumericJsonField(
                "items.body.amount.subtotal",
                "items",
                "body",
                List.of("amount", "subtotal"),
                SearchValueType.NUMBER,
                SearchModels.COMPARABLE_OPERATORS),
            SearchModels.joinedNumericJsonField(
                "items.body.amount.tax",
                "items",
                "body",
                List.of("amount", "tax"),
                SearchValueType.NUMBER,
                SearchModels.COMPARABLE_OPERATORS),
            SearchModels.joinedJsonRootField(
                "items.body",
                "items",
                "body",
                SearchValueType.STRING,
                SearchModels.STRING_OPERATORS)));
  }
}
