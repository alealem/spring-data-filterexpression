package com.example.search.jpa;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;

final class SearchableFieldDiscovery {

  private SearchableFieldDiscovery() {}

  static <T> List<SearchFieldRoot<T>> discover(Class<T> rootType) {
    List<SearchFieldRoot<T>> roots = new ArrayList<>();
    discover(rootType, List.of(), new ArrayDeque<>(), roots);
    return List.copyOf(roots);
  }

  private static <T> void discover(
      Class<?> currentType,
      List<String> pathSegments,
      Deque<Class<?>> traversalStack,
      List<SearchFieldRoot<T>> roots) {
    if (!traversalStack.isEmpty() && traversalStack.contains(currentType)) {
      return;
    }

    traversalStack.push(currentType);
    for (Field field : declaredFields(currentType)) {
      if (shouldSkip(field)) {
        continue;
      }

      Searchable searchable = field.getAnnotation(Searchable.class);
      String segment = segmentName(field, searchable);
      List<String> currentPath = append(pathSegments, segment);

      if (searchable != null && searchable.enabled()) {
        SearchValueType valueType = resolveValueType(field, searchable);
        roots.add(
            SearchModels.propertyField(
                String.join(".", currentPath), valueType, resolveOperators(searchable, valueType)));
      }

      if (shouldTraverse(field)) {
        discover(field.getType(), currentPath, traversalStack, roots);
      }
    }
    traversalStack.pop();
  }

  private static List<Field> declaredFields(Class<?> type) {
    Set<Field> fields = new LinkedHashSet<>();
    for (Class<?> current = type;
        current != null && current != Object.class;
        current = current.getSuperclass()) {
      for (Field field : current.getDeclaredFields()) {
        fields.add(field);
      }
    }
    return List.copyOf(fields);
  }

  private static boolean shouldSkip(Field field) {
    int modifiers = field.getModifiers();
    return Modifier.isStatic(modifiers)
        || Modifier.isTransient(modifiers)
        || field.isAnnotationPresent(Transient.class);
  }

  private static String segmentName(Field field, Searchable searchable) {
    if (searchable == null || searchable.path().isBlank()) {
      return field.getName();
    }
    return searchable.path();
  }

  private static SearchValueType resolveValueType(Field field, Searchable searchable) {
    if (searchable.valueType() != SearchValueType.UNKNOWN) {
      return searchable.valueType();
    }

    SearchValueType inferred = inferValueType(field.getType());
    if (inferred == SearchValueType.UNKNOWN) {
      throw new IllegalArgumentException(
          "Cannot infer searchable type for field "
              + field.getDeclaringClass().getName()
              + "."
              + field.getName());
    }
    return inferred;
  }

  private static Set<String> resolveOperators(Searchable searchable, SearchValueType valueType) {
    return searchable.operators().length == 0
        ? valueType.defaultOperators()
        : Set.of(searchable.operators());
  }

  private static SearchValueType inferValueType(Class<?> fieldType) {
    if (fieldType == String.class || CharSequence.class.isAssignableFrom(fieldType)) {
      return SearchValueType.STRING;
    }
    if (fieldType == UUID.class) {
      return SearchValueType.UUID;
    }
    if (fieldType.isEnum()) {
      return SearchValueType.STRING;
    }
    if (fieldType == boolean.class || fieldType == Boolean.class) {
      return SearchValueType.BOOLEAN;
    }
    if (fieldType == LocalDate.class) {
      return SearchValueType.DATE;
    }
    if (fieldType == Instant.class
        || fieldType == LocalDateTime.class
        || fieldType == OffsetDateTime.class
        || fieldType == ZonedDateTime.class
        || Date.class.isAssignableFrom(fieldType)) {
      return SearchValueType.DATETIME;
    }
    if (fieldType.isPrimitive() && fieldType != boolean.class && fieldType != void.class) {
      return SearchValueType.NUMBER;
    }
    if (Number.class.isAssignableFrom(fieldType)
        || fieldType == BigDecimal.class
        || fieldType == BigInteger.class) {
      return SearchValueType.NUMBER;
    }
    return SearchValueType.UNKNOWN;
  }

  private static boolean shouldTraverse(Field field) {
    if (field.isAnnotationPresent(OneToMany.class)
        || field.isAnnotationPresent(ManyToMany.class)
        || field.isAnnotationPresent(ElementCollection.class)) {
      return false;
    }
    if (Collection.class.isAssignableFrom(field.getType())
        || Map.class.isAssignableFrom(field.getType())
        || field.getType().isArray()) {
      return false;
    }
    if (field.isAnnotationPresent(ManyToOne.class)
        || field.isAnnotationPresent(OneToOne.class)
        || field.isAnnotationPresent(Embedded.class)
        || field.isAnnotationPresent(EmbeddedId.class)) {
      return true;
    }
    return isJpaStructuredType(field.getType());
  }

  private static boolean isJpaStructuredType(Class<?> fieldType) {
    return fieldType.isAnnotationPresent(Entity.class)
        || fieldType.isAnnotationPresent(Embeddable.class)
        || fieldType.isAnnotationPresent(MappedSuperclass.class);
  }

  private static List<String> append(List<String> pathSegments, String segment) {
    List<String> path = new ArrayList<>(pathSegments.size() + 1);
    path.addAll(pathSegments);
    path.addAll(FieldPathNormalizer.expandSegments(List.of(Objects.requireNonNull(segment))));
    return List.copyOf(path);
  }
}
