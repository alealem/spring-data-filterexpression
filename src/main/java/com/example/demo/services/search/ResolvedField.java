package com.example.demo.services.search;

import java.util.Set;

import jakarta.persistence.criteria.Expression;

public record ResolvedField(
    Expression<?> expression, SearchValueType valueType, Set<String> supportedOperators) {}
