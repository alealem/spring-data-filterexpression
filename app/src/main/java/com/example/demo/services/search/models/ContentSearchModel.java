package com.example.demo.services.search.models;

import org.springframework.stereotype.Component;

import com.example.demo.domain.Content;
import com.example.search.jpa.SearchModel;

@Component
public class ContentSearchModel extends SearchModel<Content> {

  public ContentSearchModel() {
    super(
        SearchModel.<Content>builder()
            .autoDiscoverSearchable(Content.class)
            .joinedJson("items.body")
            .dateTime("metadata.createdAt")
            .number("amount.total")
            .number("amount.subtotal")
            .number("amount.tax")
            .stringTree());
  }
}
