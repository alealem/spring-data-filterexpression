package com.example.demo.services.search.models;

import org.springframework.stereotype.Component;

import com.example.demo.domain.ContentItem;
import com.example.search.jpa.SearchModel;

@Component
public class ContentItemSearchModel extends SearchModel<ContentItem> {

  public ContentItemSearchModel() {
    super(
        SearchModel.<ContentItem>builder()
            .autoDiscoverSearchable(ContentItem.class)
            .json("body")
            .dateTime("metadata.createdAt")
            .number("amount.total")
            .number("amount.subtotal")
            .number("amount.tax")
            .stringTree());
  }
}
