package com.example.search.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

class FilterSpecBuilderTest {

  @Test
  void shouldSearchStringValues() {
    @SuppressWarnings("unchecked")
    Expression<String> expression = mock(Expression.class);
    @SuppressWarnings("unchecked")
    Expression<String> stringExpression = mock(Expression.class);
    @SuppressWarnings("unchecked")
    Expression<String> lowered = mock(Expression.class);
    Predicate predicate = mock(Predicate.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);

    when(expression.as(String.class)).thenReturn(stringExpression);
    when(cb.lower(stringExpression)).thenReturn(lowered);
    when(cb.like(lowered, "invoice")).thenReturn(predicate);

    Predicate result =
        specificationFor(
                new ValueExpression(FilterOperators.LIKE, false, "body.type", "Invoice", false),
                modelFor("body.type", SearchValueType.STRING, FilterOperators.STRING_OPERATORS, expression))
            .toPredicate(mockRoot(), mockQuery(), cb);

    assertSame(predicate, result);
    verify(cb).like(lowered, "invoice");
  }

  @Test
  void shouldSearchNumberValues() {
    @SuppressWarnings("unchecked")
    Expression<BigDecimal> expression = mock(Expression.class);
    Predicate predicate = mock(Predicate.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    BigDecimal total = new BigDecimal("1428.59");

    when(cb.greaterThan(expression, total)).thenReturn(predicate);

    Predicate result =
        specificationFor(
                new ValueExpression(
                    FilterOperators.GREATER, false, "body.amount.total", "1428.59", null),
                modelFor(
                    "body.amount.total",
                    SearchValueType.NUMBER,
                    FilterOperators.COMPARABLE_OPERATORS,
                    expression))
            .toPredicate(mockRoot(), mockQuery(), cb);

    assertSame(predicate, result);
    verify(cb).greaterThan(expression, total);
  }

  @Test
  void shouldSearchBooleanValues() {
    @SuppressWarnings("unchecked")
    Expression<Boolean> expression = mock(Expression.class);
    Predicate predicate = mock(Predicate.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);

    when(cb.equal(expression, true)).thenReturn(predicate);

    Predicate result =
        specificationFor(
                new ValueExpression(
                    FilterOperators.EQUAL, false, "body.metadata.approved", "true", null),
                modelFor(
                    "body.metadata.approved",
                    SearchValueType.BOOLEAN,
                    Set.of(FilterOperators.EQUAL, FilterOperators.IN),
                    expression))
            .toPredicate(mockRoot(), mockQuery(), cb);

    assertSame(predicate, result);
    verify(cb).equal(expression, true);
  }

  @Test
  void shouldSearchUuidValues() {
    UUID uuid = UUID.fromString("c4fa8bf3-538c-4a4d-b4e4-8a1ee169c8ab");
    @SuppressWarnings("unchecked")
    Expression<UUID> expression = mock(Expression.class);
    Predicate predicate = mock(Predicate.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);

    when(cb.equal(expression, uuid)).thenReturn(predicate);

    Predicate result =
        specificationFor(
                new ValueExpression(
                    FilterOperators.EQUAL, false, "body.customer.id", uuid.toString(), null),
                modelFor(
                    "body.customer.id",
                    SearchValueType.UUID,
                    FilterOperators.STRING_OPERATORS,
                    expression))
            .toPredicate(mockRoot(), mockQuery(), cb);

    assertSame(predicate, result);
    verify(cb).equal(expression, uuid);
  }

  @Test
  void shouldSearchDateValues() {
    @SuppressWarnings("unchecked")
    Expression<LocalDate> expression = mock(Expression.class);
    Predicate predicate = mock(Predicate.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    LocalDate from = LocalDate.parse("2024-12-01");
    LocalDate to = LocalDate.parse("2024-12-31");

    when(cb.between(expression, from, to)).thenReturn(predicate);

    Predicate result =
        specificationFor(
                new BetweenExpression(
                    FilterOperators.BETWEEN,
                    false,
                    "body.invoiceDate",
                    "2024-12-01",
                    "2024-12-31"),
                modelFor(
                    "body.invoiceDate",
                    SearchValueType.DATE,
                    FilterOperators.COMPARABLE_OPERATORS,
                    expression))
            .toPredicate(mockRoot(), mockQuery(), cb);

    assertSame(predicate, result);
    verify(cb).between(expression, from, to);
  }

  @Test
  void shouldSearchDateTimeValues() {
    @SuppressWarnings("unchecked")
    Expression<OffsetDateTime> expression = mock(Expression.class);
    Predicate predicate = mock(Predicate.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    OffsetDateTime dateTime = OffsetDateTime.parse("2024-12-01T10:12:45Z");

    when(cb.greaterThan(expression, dateTime)).thenReturn(predicate);

    Predicate result =
        specificationFor(
                new ValueExpression(
                    FilterOperators.GREATER,
                    false,
                    "body.metadata.createdAt",
                    "2024-12-01T10:12:45Z",
                    null),
                modelFor(
                    "body.metadata.createdAt",
                    SearchValueType.DATETIME,
                    FilterOperators.COMPARABLE_OPERATORS,
                    expression))
            .toPredicate(mockRoot(), mockQuery(), cb);

    assertSame(predicate, result);
    verify(cb).greaterThan(expression, dateTime);
  }

  private static Specification<Object> specificationFor(
      FilterExpression expression, SearchModel<Object> model) {
    return FilterSpecBuilder.toSpecification(expression, model);
  }

  private static SearchModel<Object> modelFor(
      String path,
      SearchValueType valueType,
      Set<String> operators,
      Expression<?> expression) {
    return SearchModel.<Object>builder()
        .field(path, valueType, operators, (root, query, cb, field, consumed) -> expression)
        .build();
  }

  @SuppressWarnings("unchecked")
  private static Root<Object> mockRoot() {
    return mock(Root.class);
  }

  @SuppressWarnings("unchecked")
  private static CriteriaQuery<Object> mockQuery() {
    return mock(CriteriaQuery.class);
  }
}
