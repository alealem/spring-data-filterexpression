package com.example.demo.controllers.dto;

import java.util.List;
import java.util.UUID;

public record ContentDto(
        UUID id,
        String title,
        String description,
        List<ContentItemDto> items
) {}