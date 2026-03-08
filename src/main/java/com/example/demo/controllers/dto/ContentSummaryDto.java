package com.example.demo.controllers.dto;

import java.util.UUID;

public record ContentSummaryDto(
        UUID id,
        String title,
        String description
) {}
