package com.example.demo.services.search.models;

import com.example.demo.domain.Content;
import com.example.demo.services.search.SearchModel;
import com.example.demo.services.search.SearchModels;
import com.example.demo.services.search.SearchValueType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContentSearchModel extends SearchModel<Content> {

  public ContentSearchModel() {
    super(List.of(
            SearchModels.directField(
                    "id",
                    SearchValueType.UUID,
                    SearchModels.STRING_OPERATORS
            ),
            SearchModels.directField(
                    "title",
                    SearchValueType.STRING,
                    SearchModels.STRING_OPERATORS
            ),
            SearchModels.directField(
                    "description",
                    SearchValueType.STRING,
                    SearchModels.STRING_OPERATORS
            ),

            // numeric JSON roots under items.body
            SearchModels.joinedNumericJsonRootField(
                    "items.body.amount.total",
                    "items",
                    "body",
                    SearchValueType.NUMBER,
                    SearchModels.COMPARABLE_OPERATORS
            ),
            SearchModels.joinedNumericJsonRootField(
                    "items.body.amount.subtotal",
                    "items",
                    "body",
                    SearchValueType.NUMBER,
                    SearchModels.COMPARABLE_OPERATORS
            ),
            SearchModels.joinedNumericJsonRootField(
                    "items.body.amount.tax",
                    "items",
                    "body",
                    SearchValueType.NUMBER,
                    SearchModels.COMPARABLE_OPERATORS
            ),

            // generic string JSON root under items.body
            SearchModels.joinedJsonRootField(
                    "items.body",
                    "items",
                    "body",
                    SearchValueType.STRING,
                    SearchModels.STRING_OPERATORS
            )
    ));
  }
}