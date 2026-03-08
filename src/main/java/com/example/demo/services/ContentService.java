package com.example.demo.services;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.controllers.dto.AddContentItemRequest;
import com.example.demo.controllers.dto.ContentDto;
import com.example.demo.controllers.dto.ContentItemDto;
import com.example.demo.controllers.dto.ContentMapper;
import com.example.demo.controllers.dto.ContentSummaryDto;
import com.example.demo.controllers.dto.CreateContentRequest;
import com.example.demo.controllers.dto.UpsertContentRequest;
import com.example.demo.controllers.utils.PaginatedResult;
import com.example.demo.controllers.utils.Pagination;
import com.example.demo.domain.Content;
import com.example.demo.domain.ContentItem;
import com.example.demo.errors.ResourceNotFoundException;
import com.example.demo.repositories.ContentItemRepository;
import com.example.demo.repositories.ContentRepository;
import com.example.demo.services.search.FilterExpression;
import com.example.demo.services.search.FilterSpecBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ContentService {

  private final ContentRepository contentRepo;
  private final ContentItemRepository itemRepo;
  private final ContentMapper mapper;
  private final ObjectMapper objectMapper;

  public ContentService(
      ContentRepository contentRepo,
      ContentItemRepository itemRepo,
      ContentMapper mapper,
      ObjectMapper objectMapper) {
    this.contentRepo = contentRepo;
    this.itemRepo = itemRepo;
    this.mapper = mapper;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public UUID seed() {
    UUID contentId = UUID.randomUUID();
    Content content = new Content(contentId, "Demo Content", "test description");

    content.addItem(
        new ContentItem(
            UUID.randomUUID(), json("type", "paragraph", "text", "Invoice 2023 is paid")));
    content.addItem(
        new ContentItem(
            UUID.randomUUID(), json("type", "paragraph", "text", "integration pending")));
    content.addItem(
        new ContentItem(
            UUID.randomUUID(), json("type", "note", "text", "Heidelberg meeting next week 2023")));

    contentRepo.save(content);
    return contentId;
  }

  @Transactional(readOnly = true)
  public PaginatedResult<ContentSummaryDto> listContents(Pageable pageable) {
    var page = contentRepo.findAll(pageable);
    return Pagination.from(page, mapper::toSummaryDto);
  }

  @Transactional(readOnly = true)
  public ContentDto getContent(UUID contentId) {
    Content content = requireContent(contentId);
    return mapper.toDto(content);
  }

  @Transactional(readOnly = true)
  public ContentDto getContentWithItems(UUID contentId) {
    Content content =
        contentRepo
            .findWithItemsById(contentId)
            .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + contentId));

    return mapper.toDto(content);
  }

  @Transactional
  public ContentDto createContent(CreateContentRequest request) {
    Content content = new Content(UUID.randomUUID(), request.title(), request.description());

    if (request.items() != null) {
      request
          .items()
          .forEach(
              item ->
                  content.addItem(
                      new ContentItem(UUID.randomUUID(), objectMapper.valueToTree(item.body()))));
    }

    Content saved = contentRepo.save(content);
    return mapper.toDto(saved);
  }

  @Transactional
  public ContentDto updateContent(UUID contentId, UpsertContentRequest request) {
    Content content = requireContent(contentId);
    content.update(request.title(), request.description());
    return mapper.toDto(content);
  }

  @Transactional
  public void deleteContent(UUID contentId) {
    Content content = requireContent(contentId);
    contentRepo.delete(content);
  }

  @Transactional(readOnly = true)
  public PaginatedResult<ContentItemDto> searchItems(UUID contentId, String q, Pageable pageable) {
    requireContentExists(contentId);

    var page = itemRepo.search(contentId, q, pageable);
    return Pagination.from(page, mapper::toItemDto);
  }

  @Transactional
  public ContentItemDto addItem(UUID contentId, AddContentItemRequest request) {
    Content content = requireContent(contentId);

    ContentItem item = new ContentItem(UUID.randomUUID(), objectMapper.valueToTree(request.body()));
    content.addItem(item);

    return mapper.toItemDto(item);
  }

  @Transactional(readOnly = true)
  public PaginatedResult<ContentItemDto> searchItems(
      UUID contentId, FilterExpression filter, Pageable pageable) {

    requireContentExists(contentId);

    Specification<ContentItem> contentSpec =
        (root, query, cb) -> cb.equal(root.get("content").get("id"), contentId);

    Specification<ContentItem> filterSpec = FilterSpecBuilder.toSpecification(filter);
    Specification<ContentItem> finalSpec = contentSpec.and(filterSpec);

    var page = itemRepo.findAll(finalSpec, pageable);
    return Pagination.from(page, mapper::toItemDto);
  }

  private Content requireContent(UUID contentId) {
    return contentRepo
        .findById(contentId)
        .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + contentId));
  }

  private void requireContentExists(UUID contentId) {
    if (!contentRepo.existsById(contentId)) {
      throw new ResourceNotFoundException("Content not found: " + contentId);
    }
  }

  private JsonNode json(String k1, String v1, String k2, String v2) {
    ObjectNode node = objectMapper.createObjectNode();
    node.put(k1, v1);
    node.put(k2, v2);
    return node;
  }
}
