package com.example.demo.controllers.dto;

import tools.jackson.databind.JsonNode;
import java.util.UUID;

public record ContentItemDto(UUID id, JsonNode body) {}
