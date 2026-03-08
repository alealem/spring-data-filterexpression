package com.example.demo.domain;

import java.util.UUID;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

@Entity
@Table(name = "content_items")
public class ContentItem {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private JsonNode body;

  protected ContentItem() {}

  public ContentItem(UUID id, JsonNode body) {
    this.id = id;
    this.body = body;
  }

  void setContent(Content content) {
    this.content = content;
  }

  public UUID getId() {
    return id;
  }

  public Content getContent() {
    return content;
  }

  public JsonNode getBody() {
    return body;
  }
}
