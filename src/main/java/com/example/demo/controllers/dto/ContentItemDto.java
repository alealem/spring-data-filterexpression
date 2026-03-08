package com.example.demo.controllers.dto;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.UUID;

public record ContentItemDto(UUID id, Map<String, Object> body) {}
