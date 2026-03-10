package com.example.demo.services.search;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class SearchModel<T> {

  private final List<SearchFieldRoot<T>> roots;

  public SearchModel(List<SearchFieldRoot<T>> roots) {
    this.roots =
        roots.stream()
            .sorted(
                Comparator.comparingInt((SearchFieldRoot<T> r) -> r.pathPrefix().length())
                    .reversed())
            .toList();
  }

  public ResolvedField resolve(
      Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb, FieldPathDto field) {
    List<String> segs = field == null ? List.of() : field.segments();
    if (segs.isEmpty()) {
      throw new IllegalArgumentException("field is required");
    }

    String fullPath = String.join(".", segs);

    for (SearchFieldRoot<T> candidate : roots) {
      String prefix = candidate.pathPrefix();

      if (Objects.equals(prefix, fullPath)) {
        return new ResolvedField(
            candidate.target().resolve(root, query, cb, field, segs.size()),
            candidate.valueType(),
            candidate.supportedOperators());
      }

      if (fullPath.startsWith(prefix + ".")) {
        int consumed = prefix.split("\\.").length;
        return new ResolvedField(
            candidate.target().resolve(root, query, cb, field, consumed),
            candidate.valueType(),
            candidate.supportedOperators());
      }
    }

    throw new IllegalArgumentException("Unknown filter field: " + fullPath);
  }
}
