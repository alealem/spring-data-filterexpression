package com.example.demo.controllers;

import com.example.demo.controllers.dto.ContentDto;
import com.example.demo.controllers.dto.ContentItemDto;
import com.example.demo.controllers.dto.ContentMapper;
import com.example.demo.controllers.dto.ContentSummaryDto;
import com.example.demo.controllers.utils.PaginatedResult;
import com.example.demo.controllers.utils.Pagination;
import com.example.demo.domain.Content;
import com.example.demo.domain.ContentItem;
import com.example.demo.repositories.ContentItemRepository;
import com.example.demo.repositories.ContentRepository;
import com.example.demo.services.ContentService;
import com.example.demo.services.search.FilterExpression;
import com.example.demo.services.search.FilterSpecBuilder;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ContentController {

    private final ContentService contentService;
    private final ContentRepository contentRepo;
    private final ContentItemRepository itemRepo;
    private final ObjectMapper objectMapper;
    private final ContentMapper mapper;

    public ContentController(
            ContentService contentService, ContentRepository contentRepo,
            ContentItemRepository itemRepo,
            ObjectMapper objectMapper, ContentMapper mapper
    ) {
        this.contentService = contentService;
        this.contentRepo = contentRepo;
        this.itemRepo = itemRepo;
        this.objectMapper = objectMapper;
        this.mapper = mapper;
    }

    @PostMapping("/demo/seed")
    @ResponseStatus(HttpStatus.CREATED)
    public UUID seed() {
        UUID contentId = UUID.randomUUID();
        Content content = new Content(contentId, "Demo Content", "test description");

        content.addItem(new ContentItem(
                UUID.randomUUID(),
                json("type", "paragraph", "text", "Invoice 2023 is paid")
        ));
        content.addItem(new ContentItem(
                UUID.randomUUID(),
                json("type", "paragraph", "text", "integration pending")
        ));
        content.addItem(new ContentItem(
                UUID.randomUUID(),
                json("type", "note", "text", "Heidelberg meeting next week 2023")
        ));

        contentRepo.save(content);
        return contentId;
    }

    // GET /api/contents
    @GetMapping("/contents")
    public PaginatedResult<ContentSummaryDto> listContents(Pageable pageable) {
        var page = contentRepo.findAll(pageable);
        return Pagination.from(page, mapper::toSummaryDto);
    }

    // GET /api/contents/{contentId}
    @GetMapping("/contents/{contentId}")
    public ContentDto getContent(@PathVariable UUID contentId) {
        Content content = contentRepo.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("Content not found: " + contentId));
        return mapper.toDto(content);
    }

    @GetMapping("/contents-with-itmes/{contentId}")
    public ContentDto getContentWithItems(@PathVariable UUID contentId) {


        return contentService.getContentWithItems(contentId);
    }

    // POST /api/contents
    @PostMapping("/contents")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentDto createContent(@RequestBody CreateContentRequest request) {
        Content content = new Content(
                UUID.randomUUID(),
                request.title(),
                request.description()
        );

        if (request.items() != null) {
            request.items().forEach(item ->
                    content.addItem(
                            new ContentItem(
                                    UUID.randomUUID(),
                                    objectMapper.valueToTree(item.body())
                            )
                    )
            );
        }

        Content saved = contentRepo.save(content);
        return mapper.toDto(saved);
    }

    // PUT /api/contents/{contentId}
    @PutMapping("/contents/{contentId}")
    public ContentDto updateContent(
            @PathVariable UUID contentId,
            @RequestBody UpsertContentRequest request
    ) {
        Content content = contentRepo.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("Content not found: " + contentId));

        content.update(request.title(), request.description());

        Content saved = contentRepo.save(content);
        return mapper.toDto(saved);
    }

    // DELETE /api/contents/{contentId}
    @DeleteMapping("/contents/{contentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContent(@PathVariable UUID contentId) {
        if (!contentRepo.existsById(contentId)) {
            throw new EntityNotFoundException("Content not found: " + contentId);
        }
        contentRepo.deleteById(contentId);
    }

    // GET /api/contents/{contentId}/items?q=invoice&page=0&size=10
    @GetMapping("/contents/{contentId}/items")
    public PaginatedResult<ContentItemDto> searchItems(
            @PathVariable UUID contentId,
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        var page = itemRepo.search(contentId, q, pageable);
        return Pagination.from(page, mapper::toItemDto);
    }
    @PostMapping("/contents/{contentId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentItemDto addItem(
            @PathVariable UUID contentId,
            @RequestBody AddContentItemRequest request
    ) {

        return contentService.addItem(contentId, request);
    }
    // POST /api/contents/{contentId}/items/search
    @PostMapping("/contents/{contentId}/items/search")
    public PaginatedResult<ContentItemDto> searchItems(
            @PathVariable UUID contentId,
            @RequestBody FilterExpression filter,
            Pageable pageable
    ) {
        Specification<ContentItem> contentSpec = (root, query, cb) ->
                cb.equal(root.get("content").get("id"), contentId);

        Specification<ContentItem> filterSpec = FilterSpecBuilder.toSpecification(filter);
        Specification<ContentItem> finalSpec = contentSpec.and(filterSpec);

        var page = itemRepo.findAll(finalSpec, pageable);
        return Pagination.from(page, mapper::toItemDto);
    }

    private JsonNode json(String k1, String v1, String k2, String v2) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put(k1, v1);
        node.put(k2, v2);
        return node;
    }

    public record UpsertContentRequest(
            String title,
            String description
    ) {}

    public record CreateContentRequest(
            String title,
            String description,
            List<CreateContentItemRequest> items
    ) {}
    public record CreateContentItemRequest(
            Map<String, Object> body
    ) {}

    public record AddContentItemRequest(
            Map<String, Object> body
    ) {}
}