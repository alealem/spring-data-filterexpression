package com.example.demo.services;

import com.example.demo.controllers.ContentController;
import com.example.demo.controllers.dto.ContentDto;
import com.example.demo.controllers.dto.ContentItemDto;
import com.example.demo.controllers.dto.ContentMapper;
import com.example.demo.domain.Content;
import com.example.demo.domain.ContentItem;
import com.example.demo.repositories.ContentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ContentService {

    private final ContentRepository contentRepo;
    private final ContentMapper mapper;
    private final ObjectMapper objectMapper;

    public ContentService(ContentRepository contentRepo, ContentMapper mapper, ObjectMapper objectMapper) {
        this.contentRepo = contentRepo;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ContentDto getContentWithItems(UUID contentId) {
        Content content = contentRepo.findWithItemsById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("Content not found: " + contentId));

        return mapper.toDto(content);
    }

    @Transactional
    public ContentItemDto addItem(UUID contentId, ContentController.AddContentItemRequest request) {
        Content content = contentRepo.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("Content not found: " + contentId));

        ContentItem item = new ContentItem(
                UUID.randomUUID(),
                objectMapper.valueToTree(request.body())
        );

        content.addItem(item);

        return mapper.toItemDto(item);
    }
}
