package com.example.search.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Searchable {

  SearchValueType valueType() default SearchValueType.UNKNOWN;

  String[] operators() default {};

  String path() default "";

  boolean enabled() default true;
}
