# search-jpa

`search-jpa` is a small Spring Data JPA library for building reusable filter/search APIs with:

- annotation-driven discovery of direct entity fields
- nested singular path support such as `content.id`
- PostgreSQL JSONB path filtering for `string`, `number`, `boolean`, `uuid`, `date`, and `datetime`
- a sealed Java 21 filter expression model

## Scope

This library is designed for:

- Spring Data JPA `Specification`-based querying
- PostgreSQL-backed applications that store part of their search surface inside `jsonb`
- APIs that accept structured filter payloads instead of hardcoded repository methods

This library does not try to infer arbitrary JSON schemas. JSON paths remain explicit by design.

## Installation

Add the dependency:

```xml
<dependency>
  <groupId>com.example</groupId>
  <artifactId>search-jpa</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

The module requires:

- Java 21
- Spring Data JPA
- PostgreSQL for JSON helper functions

## PostgreSQL Setup

When you use JSON filtering, install the helper SQL functions from:

[`src/main/resources/META-INF/search-jpa/postgresql-functions.sql`](src/main/resources/META-INF/search-jpa/postgresql-functions.sql)

Typical usage is to copy that SQL into a Flyway or Liquibase migration in the consuming application.

## Direct Fields

Mark direct entity fields with `@Searchable`:

```java
@Entity
class Content {

  @Searchable
  @Id
  private UUID id;

  @Searchable
  private String title;

  @Searchable
  private String description;
}
```

Then build a model:

```java
SearchModel<Content> model =
    SearchModel.<Content>builder()
        .autoDiscoverSearchable(Content.class)
        .build();
```

`autoDiscoverSearchable(...)` is the most convenient option when you want the entity model itself to define the searchable surface.

The discovery rules are conservative:

- singular nested paths such as `@ManyToOne`, `@OneToOne`, and embedded types are traversed
- collections such as `@OneToMany` are not auto-discovered
- unsupported annotated types fail fast during model construction

## Registration Styles

The library supports three registration styles.

### 1. Annotation-driven discovery

```java
SearchModel<Content> model =
    SearchModel.<Content>builder()
        .autoDiscoverSearchable(Content.class)
        .build();
```

Advantages:

- lowest configuration cost
- field names stay aligned with the entity model
- nested singular paths such as `content.id` are discovered automatically
- good default for internal applications

Tradeoffs:

- requires adding `@Searchable` to entity fields
- collections are still explicit
- JSON fields are still explicit

### 2. Manual typed field registration

```java
SearchModel<Content> model =
    SearchModel.<Content>builder()
        .uuidField("id")
        .stringField("title")
        .stringField("description")
        .uuidField("owner.id")
        .build();
```

Advantages:

- no entity annotations required
- explicit and easy to read
- good when the searchable API must be narrower than the entity model
- better for shared domain models you do not want to annotate

Tradeoffs:

- more manual registration
- string paths can drift if fields are renamed

### 3. Low-level custom registration

```java
SearchModel<Content> model =
    SearchModel.<Content>builder()
        .field(
            "items.id",
            SearchValueType.UUID,
            FilterOperators.STRING_OPERATORS,
            (root, query, cb, field, consumed) -> {
              query.distinct(true);
              return root.join("items").get("id");
            })
        .build();
```

Advantages:

- full control over joins, aliases, and computed expressions
- works for collection relations, custom SQL functions, and unusual mappings
- useful as an escape hatch when the higher-level builder methods are not enough

Tradeoffs:

- most verbose option
- leaks JPA criteria details into the registration
- should be used sparingly in consumer code

## Recommended Usage

In practice, most consumers should use a hybrid:

- `autoDiscoverSearchable(...)` or manual typed fields for normal scalar entity fields
- `json(...)` / `joinedJson(...)` for JSON columns
- low-level `field(...)` only for special joins or computed expressions

That usually gives the best balance between maintainability and control.

## Collection Relations

Collection relations are explicit.

Given:

```java
@Entity
class Content {

  @Searchable
  @Id
  private UUID id;

  @OneToMany(mappedBy = "content")
  private List<ContentItem> items;
}

@Entity
class ContentItem {

  @Searchable
  @Id
  private UUID id;

  @ManyToOne
  private Content content;
}
```

`items.id` will not be auto-discovered, because `items` is a collection. Register it explicitly:

```java
SearchModel<Content> model =
    SearchModel.<Content>builder()
        .autoDiscoverSearchable(Content.class)
        .uuidField("id")
        .field(
            "items.id",
            SearchValueType.UUID,
            FilterOperators.STRING_OPERATORS,
            (root, query, cb, field, consumed) -> {
              query.distinct(true);
              return root.join("items").get("id");
            })
        .joinedJson("items.body")
        .number("amount.total")
        .stringTree()
        .build();
```

That enables payloads such as:

```json
{
  "operator": "equal",
  "field": "items.id",
  "value": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
}
```

## JSON Fields

JSON registration is always explicit. The library does not attempt to infer JSON schemas from persisted data.

JSON paths are registered explicitly through a scoped builder.

Direct JSON column:

```java
SearchModel<ContentItem> model =
    SearchModel.<ContentItem>builder()
        .autoDiscoverSearchable(ContentItem.class)
        .json("body")
        .uuid("customer.id")
        .bool("metadata.approved")
        .date("invoiceDate")
        .dateTime("metadata.createdAt")
        .number("amount.total")
        .stringTree()
        .build();
```

Joined JSON column:

```java
SearchModel<Content> model =
    SearchModel.<Content>builder()
        .autoDiscoverSearchable(Content.class)
        .joinedJson("items.body")
        .dateTime("metadata.createdAt")
        .number("amount.total")
        .stringTree()
        .build();
```

Open a second scope when the entity has another JSON column:

```java
SearchModel.<ContentItem>builder()
    .autoDiscoverSearchable(ContentItem.class)
    .json("body")
    .number("amount.total")
    .stringTree()
    .json("metadata")
    .dateTime("createdAt")
    .stringTree();
```

## Building Specifications

`FilterSpecBuilder` converts a filter payload into a Spring Data JPA `Specification`.

```java
Specification<Content> spec = FilterSpecBuilder.toSpecification(filterExpression, model);
Page<Content> page = repository.findAll(spec, pageable);
```

## Example Payload

```json
{
  "operator": "and",
  "expressions": [
    {
      "operator": "equal",
      "field": "items.body.customer.contact.email",
      "value": "finance@acme.com"
    },
    {
      "operator": "greater",
      "field": "items.body.amount.total",
      "value": 1000
    },
    {
      "operator": "greater-or-equal",
      "field": "items.body.metadata.createdAt",
      "value": "2024-12-01T10:12:45Z"
    }
  ]
}
```

## Supported Types

- `STRING`
  - default operators: `equal`, `like`, `in`
- `UUID`
  - default operators: `equal`, `like`, `in`
- `NUMBER`
  - default operators: `equal`, `greater`, `greater-or-equal`, `less`, `less-or-equal`, `between`, `in`
- `BOOLEAN`
  - default operators: `equal`, `in`
- `DATE`
  - default operators: `equal`, `greater`, `greater-or-equal`, `less`, `less-or-equal`, `between`, `in`
- `DATETIME`
  - default operators: `equal`, `greater`, `greater-or-equal`, `less`, `less-or-equal`, `between`, `in`

## Public Entry Points

Most consumers only need:

- `@Searchable`
- `SearchModel`
- `SearchModelBuilder`
- `FilterExpression` and its records
- `FilterSpecBuilder`
- `SearchValueType`
- `FilterOperators`

Low-level registration through `field(...)` and `SearchFieldRoot` is still available for advanced cases.

## Current Limits

- JSON paths are PostgreSQL-specific
- collection traversal is explicit, not auto-discovered
- JSON schema discovery is intentionally not implemented
- JSON performance still depends on proper database indexing
