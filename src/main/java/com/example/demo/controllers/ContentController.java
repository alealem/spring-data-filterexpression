package com.example.demo.controllers;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.example.demo.controllers.dto.AddContentItemRequest;
import com.example.demo.controllers.dto.ContentDto;
import com.example.demo.controllers.dto.ContentItemDto;
import com.example.demo.controllers.dto.ContentSummaryDto;
import com.example.demo.controllers.dto.CreateContentRequest;
import com.example.demo.controllers.dto.UpsertContentRequest;
import com.example.demo.controllers.utils.PaginatedResult;
import com.example.demo.services.ContentService;
import com.example.demo.services.search.FilterExpression;

@RestController
@RequestMapping("/api")
public class ContentController {

  private final ContentService contentService;

  public ContentController(ContentService contentService) {
    this.contentService = contentService;
  }

  @PostMapping("/demo/seed")
  @ResponseStatus(HttpStatus.CREATED)
  public UUID seed() {
    return contentService.seed();
  }

  @GetMapping("/contents")
  public PaginatedResult<ContentSummaryDto> listContents(Pageable pageable) {
    return contentService.listContents(pageable);
  }

  @GetMapping("/contents/{contentId}")
  public ContentDto getContent(@PathVariable UUID contentId) {
    return contentService.getContent(contentId);
  }

  @PostMapping("/contents")
  @ResponseStatus(HttpStatus.CREATED)
  public ContentDto createContent(@RequestBody CreateContentRequest request) {
    return contentService.createContent(request);
  }

  @PutMapping("/contents/{contentId}")
  public ContentDto updateContent(
      @PathVariable UUID contentId, @RequestBody UpsertContentRequest request) {
    return contentService.updateContent(contentId, request);
  }

  @DeleteMapping("/contents/{contentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteContent(@PathVariable UUID contentId) {
    contentService.deleteContent(contentId);
  }

  @GetMapping("/contents/{contentId}/items")
  public PaginatedResult<ContentItemDto> searchItems(
      @PathVariable UUID contentId, @RequestParam(required = false) String q, Pageable pageable) {
    return contentService.searchItems(contentId, q, pageable);
  }

  @PostMapping("/contents/{contentId}/items")
  @ResponseStatus(HttpStatus.CREATED)
  public ContentItemDto addItem(
      @PathVariable UUID contentId, @RequestBody AddContentItemRequest request) {
    return contentService.addItem(contentId, request);
  }

  @PostMapping("/contents/{contentId}/items/search")
  public PaginatedResult<ContentItemDto> searchItems(
      @PathVariable UUID contentId, @RequestBody FilterExpression filter, Pageable pageable) {
    return contentService.searchItems(contentId, filter, pageable);
  }

  @PostMapping("/contents/search")
  public PaginatedResult<ContentDto> searchContents(
      @RequestBody FilterExpression filter, Pageable pageable) {
    return contentService.searchContents(filter, pageable);
  }
}
