package com.example.demo.services.search;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

@FunctionalInterface
public interface FieldTarget<T> {
  Expression<?> resolve(
      Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder cb,
      FieldPathDto field,
      int consumedSegments);
}
