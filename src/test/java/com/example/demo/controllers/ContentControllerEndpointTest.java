package com.example.demo.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.demo.controllers.dto.AddContentItemRequest;
import com.example.demo.controllers.dto.ContentDto;
import com.example.demo.controllers.dto.ContentItemDto;
import com.example.demo.controllers.dto.ContentSummaryDto;
import com.example.demo.controllers.dto.CreateContentRequest;
import com.example.demo.controllers.dto.UpsertContentRequest;
import com.example.demo.controllers.utils.PaginatedResult;
import com.example.demo.errors.ApiExceptionHandler;
import com.example.demo.errors.ResourceNotFoundException;
import com.example.demo.services.ContentService;
import com.example.search.jpa.AndExpression;
import com.example.search.jpa.FieldPathNormalizer;
import com.example.search.jpa.FilterExpression;
import com.example.search.jpa.OrExpression;
import com.example.search.jpa.ValueExpression;
import com.fasterxml.jackson.databind.ObjectMapper;

class ContentControllerEndpointTest {
  private static final UUID CONTENT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID ITEM_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

  private final ObjectMapper objectMapper = new ObjectMapper();
  private RecordingContentService contentService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    contentService = new RecordingContentService();
    mockMvc =
        MockMvcBuilders.standaloneSetup(new ContentController(contentService))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
  }

  @Test
  void seedShouldReturnCreatedUuid() throws Exception {
    contentService.seedResponse = CONTENT_ID;

    mockMvc
        .perform(post("/api/demo/seed"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$").value(CONTENT_ID.toString()));

    assertEquals(true, contentService.seedCalled);
  }

  @Test
  void listContentsShouldReturnPaginatedSummaries() throws Exception {
    contentService.listContentsResponse =
        new PaginatedResult<>(
            List.of(new ContentSummaryDto(CONTENT_ID, "Invoice Documents", "Invoices and notes")),
            0,
            10,
            1,
            1,
            true,
            true);

    mockMvc
        .perform(get("/api/contents?page=0&size=10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].id").value(CONTENT_ID.toString()))
        .andExpect(jsonPath("$.items[0].title").value("Invoice Documents"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(10));

    assertEquals(0, contentService.capturedPageable.getPageNumber());
    assertEquals(10, contentService.capturedPageable.getPageSize());
  }

  @Test
  void getContentShouldReturnDto() throws Exception {
    contentService.getContentResponse = sampleContentDto();

    mockMvc
        .perform(get("/api/contents/{contentId}", CONTENT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(CONTENT_ID.toString()))
        .andExpect(jsonPath("$.items[0].body.customer.contact.email").value("finance@acme.com"));

    assertEquals(CONTENT_ID, contentService.capturedContentId);
  }

  @Test
  void createContentShouldAcceptNestedInvoicePayload() throws Exception {
    String requestBody = readResource("requests/create-content-invoice.json");
    CreateContentRequest request =
        objectMapper.readValue(requestBody, CreateContentRequest.class);
    Map<String, Object> body = request.items().getFirst().body();

    contentService.createContentResponse =
        new ContentDto(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            request.title(),
            request.description(),
            List.of(
                new ContentItemDto(
                    UUID.fromString("22222222-2222-2222-2222-222222222222"), body)));

    mockMvc
        .perform(post("/api/contents").contentType(APPLICATION_JSON).content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
        .andExpect(jsonPath("$.title").value("Invoice Documents"))
        .andExpect(jsonPath("$.items[0].body.type").value("invoice"))
        .andExpect(jsonPath("$.items[0].body.customer.contact.email").value("finance@acme.com"))
        .andExpect(jsonPath("$.items[0].body.amount.total").value(1428.59))
        .andExpect(jsonPath("$.items[0].body.workflow.steps[2].step").value("approved"));

    assertNotNull(contentService.capturedCreateRequest);
    Map<String, Object> capturedBody = contentService.capturedCreateRequest.items().getFirst().body();
    assertEquals("invoice", capturedBody.get("type"));
    assertEquals(
        "finance@acme.com", nestedMap(capturedBody, "customer", "contact").get("email"));
    assertEquals(2, listValue(capturedBody, "lineItems").size());
  }

  @Test
  void updateContentShouldReturnUpdatedDto() throws Exception {
    String requestBody = """
        {
          "title": "Updated Content Title",
          "description": "Updated description"
        }
        """;

    contentService.updateContentResponse =
        new ContentDto(CONTENT_ID, "Updated Content Title", "Updated description", List.of());

    mockMvc
        .perform(
            put("/api/contents/{contentId}", CONTENT_ID)
                .contentType(APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(CONTENT_ID.toString()))
        .andExpect(jsonPath("$.title").value("Updated Content Title"))
        .andExpect(jsonPath("$.description").value("Updated description"));

    assertEquals(CONTENT_ID, contentService.capturedContentId);
    assertEquals("Updated Content Title", contentService.capturedUpsertRequest.title());
  }

  @Test
  void deleteContentShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/contents/{contentId}", CONTENT_ID)).andExpect(status().isNoContent());

    assertEquals(CONTENT_ID, contentService.capturedContentId);
    assertEquals(true, contentService.deleteCalled);
  }

  @Test
  void searchContentsShouldAcceptCollectionStyleFilterPayload() throws Exception {
    String requestBody = readResource("requests/search-contents-invoice-filter.json");

    contentService.searchContentsResponse =
        new PaginatedResult<>(
            List.of(
                new ContentDto(
                    UUID.fromString("33333333-3333-3333-3333-333333333333"),
                    "Invoice Documents",
                    "Invoices and notes",
                    List.of())),
            0,
            10,
            1,
            1,
            true,
            true);

    mockMvc
        .perform(
            post("/api/contents/search?page=0&size=10")
                .contentType(APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].id").value("33333333-3333-3333-3333-333333333333"))
        .andExpect(jsonPath("$.items[0].title").value("Invoice Documents"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.totalItems").value(1));

    assertNotNull(contentService.capturedSearchFilter);
    assertEquals(0, contentService.capturedPageable.getPageNumber());
    assertEquals(10, contentService.capturedPageable.getPageSize());

    AndExpression and = assertInstanceOf(AndExpression.class, contentService.capturedSearchFilter);
    OrExpression or = assertInstanceOf(OrExpression.class, and.expressions().getFirst());
    ValueExpression noteType = assertInstanceOf(ValueExpression.class, or.expressions().get(0));
    ValueExpression email = assertInstanceOf(ValueExpression.class, or.expressions().get(1));
    ValueExpression paragraphType =
        assertInstanceOf(ValueExpression.class, or.expressions().get(2));
    ValueExpression totalAmount = assertInstanceOf(ValueExpression.class, and.expressions().get(1));

    assertEquals(
        List.of("items", "body", "type"),
        FieldPathNormalizer.normalize(noteType.field()).segments());
    assertEquals(
        List.of("items", "body", "customer", "contact", "email"),
        FieldPathNormalizer.normalize(email.field()).segments());
    assertEquals(
        List.of("items", "body", "type"),
        FieldPathNormalizer.normalize(paragraphType.field()).segments());
    assertEquals(
        List.of("items", "body", "amount", "total"),
        FieldPathNormalizer.normalize(totalAmount.field()).segments());
  }

  @Test
  void searchItemsByQueryShouldReturnPaginatedItems() throws Exception {
    contentService.searchItemsQueryResponse =
        new PaginatedResult<>(List.of(sampleItemDto()), 0, 10, 1, 1, true, true);

    mockMvc
        .perform(get("/api/contents/{contentId}/items?q=invoice&page=0&size=10", CONTENT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].id").value(ITEM_ID.toString()))
        .andExpect(jsonPath("$.items[0].body.type").value("invoice"))
        .andExpect(jsonPath("$.items[0].body.amount.total").value(1428.59));

    assertEquals(CONTENT_ID, contentService.capturedContentId);
    assertEquals("invoice", contentService.capturedQuery);
    assertEquals(0, contentService.capturedPageable.getPageNumber());
  }

  @Test
  void addItemShouldCreateContentItem() throws Exception {
    String requestBody = """
        {
          "body": {
            "type": "invoice",
            "text": "Invoice 2024-001 approved for payment"
          }
        }
        """;

    contentService.addItemResponse =
        new ContentItemDto(
            ITEM_ID,
            Map.of("type", "invoice", "text", "Invoice 2024-001 approved for payment"));

    mockMvc
        .perform(
            post("/api/contents/{contentId}/items", CONTENT_ID)
                .contentType(APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(ITEM_ID.toString()))
        .andExpect(jsonPath("$.body.type").value("invoice"));

    assertEquals(CONTENT_ID, contentService.capturedContentId);
    assertEquals("invoice", contentService.capturedAddItemRequest.body().get("type"));
  }

  @Test
  void searchItemsByFilterShouldAcceptItemFilterPayload() throws Exception {
    String requestBody = """
        {
          "operator": "and",
          "expressions": [
            {
              "operator": "like",
              "exclude": false,
              "field": ["body", "text"],
              "value": "%invoice%",
              "caseSensitive": false
            },
            {
              "operator": "greater",
              "exclude": false,
              "field": ["body", "amount", "total"],
              "value": 1000
            }
          ]
        }
        """;

    contentService.searchItemsFilterResponse =
        new PaginatedResult<>(List.of(sampleItemDto()), 0, 10, 1, 1, true, true);

    mockMvc
        .perform(
            post("/api/contents/{contentId}/items/search?page=0&size=10", CONTENT_ID)
                .contentType(APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].id").value(ITEM_ID.toString()))
        .andExpect(jsonPath("$.items[0].body.customer.contact.email").value("finance@acme.com"));

    assertEquals(CONTENT_ID, contentService.capturedContentId);
    assertEquals(0, contentService.capturedPageable.getPageNumber());
    AndExpression and =
        assertInstanceOf(AndExpression.class, contentService.capturedSearchItemsFilter);
    ValueExpression textFilter = assertInstanceOf(ValueExpression.class, and.expressions().getFirst());
    ValueExpression totalFilter =
        assertInstanceOf(ValueExpression.class, and.expressions().get(1));
    assertEquals(
        List.of("body", "text"), FieldPathNormalizer.normalize(textFilter.field()).segments());
    assertEquals(
        List.of("body", "amount", "total"),
        FieldPathNormalizer.normalize(totalFilter.field()).segments());
  }

  @Test
  void getContentShouldReturnNotFoundWhenServiceThrows() throws Exception {
    contentService.getContentException =
        new ResourceNotFoundException("Content not found: " + CONTENT_ID);

    mockMvc
        .perform(get("/api/contents/{contentId}", CONTENT_ID))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Resource not found"))
        .andExpect(jsonPath("$.detail").value("Content not found: " + CONTENT_ID));
  }

  @Test
  void searchContentsShouldReturnBadRequestForUnsupportedFieldFormat() throws Exception {
    String requestBody = """
        {
          "operator": "equal",
          "exclude": false,
          "field": {
            "path": "items.body.type"
          },
          "value": "invoice"
        }
        """;

    mockMvc
        .perform(post("/api/contents/search").contentType(APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Bad request"))
        .andExpect(
            jsonPath("$.detail").value("Unsupported field format: class java.util.LinkedHashMap"));
  }

  @Test
  void createContentShouldReturnBadRequestForMalformedJson() throws Exception {
    mockMvc
        .perform(post("/api/contents").contentType(APPLICATION_JSON).content("{"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Malformed request"));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> nestedMap(Map<String, Object> source, String... path) {
    Map<String, Object> current = source;
    for (String segment : path) {
      current = (Map<String, Object>) current.get(segment);
    }
    return current;
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> listValue(Map<String, Object> source, String key) {
    return (List<Map<String, Object>>) source.get(key);
  }

  private ContentDto sampleContentDto() throws Exception {
    String requestBody = readResource("requests/create-content-invoice.json");
    CreateContentRequest request =
        objectMapper.readValue(requestBody, CreateContentRequest.class);
    return new ContentDto(
        CONTENT_ID,
        request.title(),
        request.description(),
        List.of(new ContentItemDto(ITEM_ID, request.items().getFirst().body())));
  }

  private ContentItemDto sampleItemDto() throws Exception {
    return sampleContentDto().items().getFirst();
  }

  private static String readResource(String resourcePath) throws IOException {
    try (var inputStream =
        ContentControllerEndpointTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (inputStream == null) {
        throw new IOException("Missing test resource: " + resourcePath);
      }
      return new String(inputStream.readAllBytes());
    }
  }

  private static final class RecordingContentService extends ContentService {

    private boolean seedCalled;
    private boolean deleteCalled;
    private UUID seedResponse;
    private UUID capturedContentId;
    private String capturedQuery;
    private CreateContentRequest capturedCreateRequest;
    private UpsertContentRequest capturedUpsertRequest;
    private AddContentItemRequest capturedAddItemRequest;
    private FilterExpression capturedSearchFilter;
    private FilterExpression capturedSearchItemsFilter;
    private Pageable capturedPageable;
    private PaginatedResult<ContentSummaryDto> listContentsResponse;
    private ContentDto getContentResponse;
    private ContentDto createContentResponse;
    private ContentDto updateContentResponse;
    private ContentItemDto addItemResponse;
    private PaginatedResult<ContentDto> searchContentsResponse;
    private PaginatedResult<ContentItemDto> searchItemsQueryResponse;
    private PaginatedResult<ContentItemDto> searchItemsFilterResponse;
    private RuntimeException getContentException;

    private RecordingContentService() {
      super(null, null, null, null, null, null);
    }

    @Override
    public UUID seed() {
      seedCalled = true;
      return seedResponse;
    }

    @Override
    public PaginatedResult<ContentSummaryDto> listContents(Pageable pageable) {
      capturedPageable = pageable;
      return listContentsResponse;
    }

    @Override
    public ContentDto getContent(UUID contentId) {
      capturedContentId = contentId;
      if (getContentException != null) {
        throw getContentException;
      }
      return getContentResponse;
    }

    @Override
    public ContentDto createContent(CreateContentRequest request) {
      capturedCreateRequest = request;
      return createContentResponse;
    }

    @Override
    public ContentDto updateContent(UUID contentId, UpsertContentRequest request) {
      capturedContentId = contentId;
      capturedUpsertRequest = request;
      return updateContentResponse;
    }

    @Override
    public void deleteContent(UUID contentId) {
      capturedContentId = contentId;
      deleteCalled = true;
    }

    @Override
    public PaginatedResult<ContentDto> searchContents(FilterExpression filter, Pageable pageable) {
      capturedSearchFilter = filter;
      capturedPageable = pageable;
      normalizeValueFields(filter).forEach(FieldPathNormalizer::normalize);
      return searchContentsResponse;
    }

    @Override
    public PaginatedResult<ContentItemDto> searchItems(UUID contentId, String q, Pageable pageable) {
      capturedContentId = contentId;
      capturedQuery = q;
      capturedPageable = pageable;
      return searchItemsQueryResponse;
    }

    @Override
    public ContentItemDto addItem(UUID contentId, AddContentItemRequest request) {
      capturedContentId = contentId;
      capturedAddItemRequest = request;
      return addItemResponse;
    }

    @Override
    public PaginatedResult<ContentItemDto> searchItems(
        UUID contentId, FilterExpression filter, Pageable pageable) {
      capturedContentId = contentId;
      capturedSearchItemsFilter = filter;
      capturedPageable = pageable;
      normalizeValueFields(filter).forEach(FieldPathNormalizer::normalize);
      return searchItemsFilterResponse;
    }

    private static Stream<Object> normalizeValueFields(FilterExpression expression) {
      return switch (expression) {
        case AndExpression and ->
            and.expressions().stream().flatMap(RecordingContentService::normalizeValueFields);
        case OrExpression or ->
            or.expressions().stream().flatMap(RecordingContentService::normalizeValueFields);
        case ValueExpression value -> Stream.of(value.field());
        default -> Stream.empty();
      };
    }
  }
}
