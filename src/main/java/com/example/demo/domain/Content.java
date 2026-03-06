package com.example.demo.domain;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "contents")
public class Content {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentItem> items = new ArrayList<>();

    protected Content() {}

    public Content(UUID id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description=description;
    }

    public void addItem(ContentItem item) {
        items.add(item);
        item.setContent(this);
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public List<ContentItem> getItems() { return items; }
}