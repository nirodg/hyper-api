package dev.hyperapi.runtime.core.processor.annotations;

import dev.hyperapi.runtime.core.processor.enums.HttpMethod;
import dev.hyperapi.runtime.core.processor.enums.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JPA entity to be exposed automatically by HyperAPI.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HyperResource {

  String path() default "";

  String dto() default "";

  String repositoryPackage() default "repository";

  Scope scope() default Scope.APPLICATION;

  HttpMethod[] disabledFor() default {};

  Mapping mapping() default @Mapping(ignore = {});

  Pageable pageable() default @Pageable(limit = 20, maxLimit = 100);

  Events events() default @Events(onCreate = false, onUpdate = false, onDelete = false);

  Cache cache() default @Cache(enabled = false, ttlSeconds = 60);

  Security security() default
  @Security(
      rolesAllowed = {},
      requireAuth = false);

}
