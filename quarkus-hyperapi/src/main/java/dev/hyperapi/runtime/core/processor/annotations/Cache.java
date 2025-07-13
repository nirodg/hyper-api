package dev.hyperapi.runtime.core.processor.annotations;

// TODO Not implemented yet
public @interface Cache {
  boolean enabled() default false;

  /** TTL in seconds */
  int ttlSeconds() default 60;
}
