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
package com.eorghe.hyperapi.common;

import com.eorghe.hyperapi.processor.annotations.HyperResource;
import com.eorghe.hyperapi.registry.EntityRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EntityConfigProvider is a helper bean that reads and caches @HyperResource settings for each
 * exposed entity in the application.
 *
 * <p>This class provides methods to retrieve configuration for entities and resolve
 * entity classes by their simple names.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@ApplicationScoped
public class EntityConfigProvider {

  @Inject
  private EntityRegistry registry; // Injected registry for resolving entity classes.


  private final Map<Class<?>, HyperResource> config = new ConcurrentHashMap<>(); // Cache for entity configurations.

  /**
   * Retrieves the @HyperResource configuration for a given entity class.
   *
   * <p>If the configuration is not already cached, it reads the @HyperResource annotation
   * from the entity class and caches it.
   *
   * @param cls the entity class
   * @return the @HyperResource configuration
   * @throws IllegalStateException if the entity class is not annotated with @HyperResource
   */
  public HyperResource configFor(Class<?> cls) {
    return config.computeIfAbsent(
        cls,
        c -> {
          HyperResource ann = c.getAnnotation(HyperResource.class);
          if (ann == null) {
            throw new IllegalStateException("Entity not @HyperResource: " + c);
          }
          return ann;
        });
  }

  /**
   * Resolves an entity class by its simple name.
   *
   * <p>Uses the injected EntityRegistry to find the entity class. Throws a NotFoundException
   * if the entity cannot be resolved.
   *
   * @param simple the simple name of the entity
   * @return the resolved entity class
   * @throws NotFoundException if the entity is not found
   */
  public Class<?> resolve(String simple) {
    return registry
        .bySimpleName(simple)
        .orElseThrow(() -> new NotFoundException("Entity not found: " + simple));
  }
}
