package com.example.demo.controllers.dto;

import java.util.List;

public record CreateContentRequest(
    String title, String description, List<CreateContentItemRequest> items) {}
