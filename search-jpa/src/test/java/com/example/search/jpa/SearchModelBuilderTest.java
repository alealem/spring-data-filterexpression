package com.example.search.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

class SearchModelBuilderTest {

  @Test
  void shouldPreferMostSpecificPrefix() {
    AtomicInteger consumedSegments = new AtomicInteger(-1);

    SearchModel<Object> model =
        SearchModel.<Object>builder()
            .field(
                "items.body",
                SearchValueType.STRING,
                FilterOperators.STRING_OPERATORS,
                (root, query, cb, field, consumed) -> {
                  consumedSegments.set(consumed);
                  return null;
                })
            .field(
                "items",
                SearchValueType.STRING,
                FilterOperators.STRING_OPERATORS,
                (root, query, cb, field, consumed) -> {
                  throw new AssertionError("Less specific prefix should not be chosen");
                })
            .build();

    ResolvedField resolved =
        model.resolve(null, null, null, FieldPathNormalizer.normalize("items.body.type"));

    assertEquals(null, resolved.expression());
    assertEquals(2, consumedSegments.get());
  }

  @Test
  void shouldResolveNestedPropertyFieldWithoutCustomTarget() {
    @SuppressWarnings("unchecked")
    Root<Object> root = mock(Root.class);
    @SuppressWarnings("unchecked")
    Path<Object> contentPath = mock(Path.class);
    @SuppressWarnings("unchecked")
    Path<Object> idPath = mock(Path.class);

    when(root.get("content")).thenReturn(contentPath);
    when(contentPath.get("id")).thenReturn(idPath);

    SearchModel<Object> model =
        SearchModel.<Object>builder().uuidField("content.id").build();

    ResolvedField resolved =
        model.resolve(root, null, null, FieldPathNormalizer.normalize("content.id"));

    assertSame(idPath, resolved.expression());
    assertEquals(SearchValueType.UUID, resolved.valueType());
    assertEquals(FilterOperators.STRING_OPERATORS, resolved.supportedOperators());
    verify(root).get("content");
    verify(contentPath).get("id");
  }

  @Test
  void shouldResolveNumberJsonFieldFromSinglePathString() {
    @SuppressWarnings("unchecked")
    Root<Object> root = mock(Root.class);
    @SuppressWarnings("unchecked")
    Path<Object> bodyPath = mock(Path.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    @SuppressWarnings("unchecked")
    Expression<String> amountLiteral = mock(Expression.class);
    @SuppressWarnings("unchecked")
    Expression<String> totalLiteral = mock(Expression.class);
    @SuppressWarnings("unchecked")
    Expression<BigDecimal> result = mock(Expression.class);

    when(root.get("body")).thenReturn(bodyPath);
    when(cb.literal("amount")).thenReturn(amountLiteral);
    when(cb.literal("total")).thenReturn(totalLiteral);
    when(
            cb.function(
                eq("jsonb_extract_path_numeric"),
                eq(BigDecimal.class),
                any(Expression[].class)))
        .thenReturn(result);

    SearchModel<Object> model =
        SearchModel.<Object>builder().numberJsonField("body.amount.total").build();

    ResolvedField resolved =
        model.resolve(root, null, cb, FieldPathNormalizer.normalize("body.amount.total"));

    assertSame(result, resolved.expression());
    assertEquals(SearchValueType.NUMBER, resolved.valueType());
    assertEquals(FilterOperators.COMPARABLE_OPERATORS, resolved.supportedOperators());
    verify(root).get("body");
    verify(cb).literal("amount");
    verify(cb).literal("total");
  }

  @Test
  void shouldRejectInvalidJoinedJsonStringFieldPath() {
    IllegalArgumentException ex =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> SearchModel.<Object>builder().joinedJsonStringField("items"));

    assertEquals(
        "joinedJsonStringField expects '<join>.<jsonColumn>' but got: items",
        ex.getMessage());
  }

  @Test
  void shouldBuildJsonScopesFromSingleRootDeclaration() {
    SearchModelBuilder<Object> directBuilder =
        SearchModel.<Object>builder()
            .json("body")
            .uuid("customer.id")
            .bool("metadata.approved")
            .date("invoiceDate")
            .dateTime("metadata.createdAt")
            .number("amount.total")
            .number("amount.subtotal")
            .number("amount.tax")
            .stringTree();

    assertEquals(
        List.of(
            "body.customer.id",
            "body.metadata.approved",
            "body.invoiceDate",
            "body.metadata.createdAt",
            "body.amount.total",
            "body.amount.subtotal",
            "body.amount.tax",
            "body"),
        directBuilder.buildRoots().stream().map(SearchFieldRoot::pathPrefix).toList());
    assertEquals(
        List.of(
            SearchValueType.UUID,
            SearchValueType.BOOLEAN,
            SearchValueType.DATE,
            SearchValueType.DATETIME,
            SearchValueType.NUMBER,
            SearchValueType.NUMBER,
            SearchValueType.NUMBER,
            SearchValueType.STRING),
        directBuilder.buildRoots().stream().map(SearchFieldRoot::valueType).toList());

    SearchModelBuilder<Object> joinedBuilder =
        SearchModel.<Object>builder()
            .joinedJson("items.body")
            .uuid("customer.id")
            .bool("metadata.approved")
            .date("invoiceDate")
            .dateTime("metadata.createdAt")
            .number("amount.total")
            .number("amount.subtotal")
            .number("amount.tax")
            .stringTree();

    assertEquals(
        List.of(
            "items.body.customer.id",
            "items.body.metadata.approved",
            "items.body.invoiceDate",
            "items.body.metadata.createdAt",
            "items.body.amount.total",
            "items.body.amount.subtotal",
            "items.body.amount.tax",
            "items.body"),
        joinedBuilder.buildRoots().stream().map(SearchFieldRoot::pathPrefix).toList());
  }

  @Test
  void shouldResolveDateTimeJsonFieldFromSinglePathString() {
    @SuppressWarnings("unchecked")
    Root<Object> root = mock(Root.class);
    @SuppressWarnings("unchecked")
    Path<Object> bodyPath = mock(Path.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    @SuppressWarnings("unchecked")
    Expression<String> metadataLiteral = mock(Expression.class);
    @SuppressWarnings("unchecked")
    Expression<String> createdAtLiteral = mock(Expression.class);
    @SuppressWarnings("unchecked")
    Expression<OffsetDateTime> result = mock(Expression.class);

    when(root.get("body")).thenReturn(bodyPath);
    when(cb.literal("metadata")).thenReturn(metadataLiteral);
    when(cb.literal("createdAt")).thenReturn(createdAtLiteral);
    when(
            cb.function(
                eq("jsonb_extract_path_tstz"),
                eq(OffsetDateTime.class),
                any(Expression[].class)))
        .thenReturn(result);

    SearchModel<Object> model =
        SearchModel.<Object>builder().dateTimeJsonField("body.metadata.createdAt").build();

    ResolvedField resolved =
        model.resolve(root, null, cb, FieldPathNormalizer.normalize("body.metadata.createdAt"));

    assertSame(result, resolved.expression());
    assertEquals(SearchValueType.DATETIME, resolved.valueType());
    assertEquals(FilterOperators.COMPARABLE_OPERATORS, resolved.supportedOperators());
  }

  @Test
  void shouldRejectInvalidJoinedJsonRootPath() {
    IllegalArgumentException ex =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> SearchModel.<Object>builder().joinedJson("items"));

    assertEquals("joinedJson expects '<join>.<jsonColumn>' but got: items", ex.getMessage());
  }

  @Test
  void shouldAutoDiscoverAnnotatedDirectAndNestedSingularFields() {
    SearchModelBuilder<TestItem> builder =
        SearchModel.<TestItem>builder().autoDiscoverSearchable(TestItem.class);

    assertEquals(
        List.of("id", "content.id", "content.title"),
        builder.buildRoots().stream().map(SearchFieldRoot::pathPrefix).toList());
    assertEquals(
        List.of(SearchValueType.UUID, SearchValueType.UUID, SearchValueType.STRING),
        builder.buildRoots().stream().map(SearchFieldRoot::valueType).toList());
  }

  @Test
  void shouldNotAutoDiscoverCollectionPaths() {
    SearchModel<TestContent> model =
        SearchModel.<TestContent>builder().autoDiscoverSearchable(TestContent.class).build();

    IllegalArgumentException ex =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> model.resolve(null, null, null, FieldPathNormalizer.normalize("items.id")));

    assertEquals("Unknown filter field: items.id", ex.getMessage());
  }

  @Test
  void shouldFailForUnsupportedAnnotatedFieldType() {
    IllegalArgumentException ex =
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> SearchModel.<UnsupportedRoot>builder().autoDiscoverSearchable(UnsupportedRoot.class));

    assertEquals(
        "Cannot infer searchable type for field "
            + UnsupportedRoot.class.getName()
            + ".payload",
        ex.getMessage());
  }

  @Entity
  static class TestContent {

    @Searchable @Id private UUID id;

    @Searchable private String title;

    @OneToMany(mappedBy = "content")
    private List<TestItem> items;
  }

  @Entity
  static class TestItem {

    @Searchable @Id private UUID id;

    @ManyToOne private TestContent content;
  }

  @Entity
  static class UnsupportedRoot {

    @Searchable private Object payload;
  }
}
