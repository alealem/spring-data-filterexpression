package com.example.demo.controllers.dto;

import com.example.demo.domain.Content;
import com.example.demo.domain.ContentItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Map;

@Component
public final class ContentMapper {
    private final ObjectMapper objectMapper;
    public ContentMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    public ContentSummaryDto toSummaryDto(Content content) {
        return new ContentSummaryDto(
                content.getId(),
                content.getTitle(),
                content.getDescription()
        );
    }

    public ContentDto toDto(Content content) {
        return new ContentDto(
                content.getId(),
                content.getTitle(),
                content.getDescription(),
                content.getItems().stream()
                        .map(this::toItemDto)
                        .toList()
        );
    }

    public ContentItemDto toItemDto(ContentItem item) {
        Map<String, Object> bodyMap = objectMapper.convertValue(
                item.getBody(),
                new TypeReference<Map<String, Object>>() {}
        );

        return new ContentItemDto(
                item.getId(),
                bodyMap
        );
    }

    public  List<ContentDto> toDtoList(List<Content> contents) {
        return contents.stream()
                .map(this::toDto)
                .toList();
    }
}
