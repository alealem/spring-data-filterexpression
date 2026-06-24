package com.example.search.jpa;

import java.util.Set;

import jakarta.persistence.criteria.Expression;

record ResolvedField(
    Expression<?> expression, SearchValueType valueType, Set<String> supportedOperators) {}
