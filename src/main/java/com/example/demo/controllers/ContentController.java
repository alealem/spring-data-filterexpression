package com.example.demo.controllers;

import com.example.demo.controllers.dto.*;
import com.example.demo.controllers.utils.PaginatedResult;
import com.example.demo.controllers.utils.Pagination;
import com.example.demo.domain.*;

import com.example.demo.repositories.ContentItemRepository;
import com.example.demo.repositories.ContentRepository;
import com.example.demo.services.search.FilterExpression;
import com.example.demo.services.search.FilterSpecBuilder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ContentController {

    private final ContentRepository contentRepo;
    private final ContentItemRepository itemRepo;
    private final ObjectMapper apiMapper;
    private final com.fasterxml.jackson.databind.ObjectMapper dbMapper;

    public ContentController(ContentRepository contentRepo, ContentItemRepository itemRepo, ObjectMapper apiMapper) {
        this.contentRepo = contentRepo;
        this.itemRepo = itemRepo;
        this.apiMapper = apiMapper;
        this.dbMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    }


    @PostMapping("/demo/seed")
    public UUID seed() {
        UUID contentId = UUID.randomUUID();
        Content content = new Content(contentId, "Demo Content","test description");

        content.addItem(new ContentItem(UUID.randomUUID(), json("type", "paragraph", "text", "Invoice 2023 is paid")));
        content.addItem(new ContentItem(UUID.randomUUID(), json("type", "paragraph", "text", "integration pending")));
        content.addItem(new ContentItem(UUID.randomUUID(), json("type", "note", "text", "Heidelberg meeting next week 2023")));

        contentRepo.save(content);
        return contentId;
    }

    // GET /api/contents/{contentId}/items?q=invoice&page=0&size=10
    @GetMapping("/contents/{contentId}/items")
    public PaginatedResult<ContentItemDto> search(
            @PathVariable UUID contentId,
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        var page = itemRepo.search(contentId, q, pageable);
        return Pagination.from(page, ci -> new ContentItemDto(ci.getId(), toApiJson(ci.getBody())));
    }

    @PostMapping("/contents/{contentId}/items/search")
    public PaginatedResult<ContentItemDto> search(@PathVariable UUID contentId, @RequestBody FilterExpression filter, Pageable pageable) {
         Specification<ContentItem> spec = FilterSpecBuilder.toSpecification(filter);
        var page = itemRepo.findAll(spec, pageable);
        return Pagination.from(page, ci -> new ContentItemDto(ci.getId(), toApiJson(ci.getBody())));
    }

    private com.fasterxml.jackson.databind.node.ObjectNode json(String k1, String v1, String k2, String v2) {
        var n = dbMapper.createObjectNode();
        n.put(k1, v1);
        n.put(k2, v2);
        return n;
    }

    private ObjectNode toApiJson(com.fasterxml.jackson.databind.JsonNode node) {
        if (node == null) return null;
        try {
            JsonNode apiNode = apiMapper.readTree(dbMapper.writeValueAsString(node));
            return (ObjectNode) apiNode;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON node between Jackson versions", e);
        }
    }
}
