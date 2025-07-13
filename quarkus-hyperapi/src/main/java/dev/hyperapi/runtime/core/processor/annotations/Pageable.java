package dev.hyperapi.runtime.core.processor.annotations;

public @interface Pageable {

  /**
   * Default page size
   */
  int limit() default 20;

  /**
   * Max page size allowed
   */
  int maxLimit() default 100;
}