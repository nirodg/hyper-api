/*
 * MIT License
 *
 * Copyright (c) 2025 Dorin Brage
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.eorghe.hyperapi.processor.annotations;

import com.eorghe.hyperapi.processor.enums.HttpMethod;
import com.eorghe.hyperapi.processor.enums.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HyperResource is an annotation for marking JPA entities to be exposed automatically by HyperAPI.
 *
 * <p>This annotation provides configuration options for defining the API path, DTO mapping,
 * repository package, scope, HTTP methods to disable, and other features such as pagination,
 * events, caching, and security.
 *
 * <p>It is annotated with `@Target(ElementType.TYPE)` to indicate that it can only be applied
 * to classes, and `@Retention(RetentionPolicy.RUNTIME)` to ensure it is available at runtime.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HyperResource {

  /**
   * Specifies the API path for the resource.
   *
   * @return the API path as a string
   */
  String path() default "";

  /**
   * Specifies the DTO class name for the resource.
   *
   * @return the DTO class name as a string
   */
  String dto() default "";

  /**
   * Specifies the package name for the repository.
   *
   * @return the repository package name as a string
   */
  String repositoryPackage() default "repository";

  /**
   * Specifies the scope of the resource.
   *
   * @return the scope of the resource, defaulting to `Scope.APPLICATION`
   */
  Scope scope() default Scope.APPLICATION;

  /**
   * Specifies the HTTP methods that are disabled for the resource.
   *
   * @return an array of disabled HTTP methods
   */
  HttpMethod[] disabledFor() default {};

  /**
   * Specifies the mapping configuration for the resource.
   *
   * @return the mapping configuration as a `Mapping` annotation
   */
  Mapping mapping() default @Mapping(ignore = {});

  /**
   * Specifies the pagination configuration for the resource.
   *
   * @return the pagination configuration as a `Pageable` annotation
   */
  Pageable pageable() default @Pageable(limit = 20, maxLimit = 100);

  /**
   * Specifies the event handling configuration for the resource.
   *
   * @return the event configuration as an `Events` annotation
   */
  Events events() default @Events(onCreate = false, onUpdate = false, onDelete = false);

  /**
   * Specifies the caching configuration for the resource.
   *
   * @return the caching configuration as a `Cache` annotation
   */
  Cache cache() default @Cache(enabled = false, ttlSeconds = 60);

  /**
   * Specifies the security configuration for the resource.
   *
   * @return the security configuration as a `Security` annotation
   */
  Security security() default
      @Security(
          rolesAllowed = {},
          requireAuth = false);

}