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
package com.eorghe.hyperapi.registry;

import com.eorghe.hyperapi.processor.annotations.HyperResource;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Entity;
import jakarta.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

/**
 * EntityRegistry is responsible for discovering and managing JPA entities annotated with
 * `@HyperResource`.
 *
 * <p>This class scans the classpath for entities annotated with `@Entity` and `@HyperResource`,
 * and provides methods to retrieve and resolve these entities by their simple names.
 *
 * <p>Key features:
 * <ul>
 *   <li>Scans configured packages or the entire classpath for annotated entities.</li>
 *   <li>Provides methods to retrieve all discovered entities or resolve them by name.</li>
 * </ul>
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@ApplicationScoped
public class EntityRegistry {

  /**
   * Configured packages to scan for entities, specified as a comma-separated list. If blank, all
   * reachable packages on the classpath are scanned.
   */
  @ConfigProperty(name = "hyperapi.scan-packages", defaultValue = "")
  String configuredPackages;

  /**
   * Set of discovered JPA entities annotated with `@HyperResource`.
   */
  private Set<Class<?>> exposed;

  /**
   * Initializes the registry by scanning for entities annotated with `@Entity` and
   * `@HyperResource`.
   *
   * <p>This method is called automatically after the bean is constructed.
   */
  @PostConstruct
  void init() {
    ConfigurationBuilder cfg = new ConfigurationBuilder().addScanners(Scanners.TypesAnnotated);

    // Configure scanning based on the provided package list
    if (!configuredPackages.isBlank()) {
      cfg.forPackages(
          Arrays.stream(configuredPackages.split(",")).map(String::trim).toArray(String[]::new));
    } // else scan everything reachable on the classpath

    Reflections reflections = new Reflections(cfg);

    // Filter entities annotated with @HyperResource
    exposed =
        reflections.getTypesAnnotatedWith(Entity.class).stream()
            .filter(c -> c.isAnnotationPresent(HyperResource.class))
            .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Retrieves all discovered entities.
   *
   * @return a set of classes representing the discovered entities
   */
  public Set<Class<?>> all() {
    return exposed;
  }

  /**
   * Finds an entity by its simple name.
   *
   * @param simple the simple name of the entity
   * @return an optional containing the entity class if found, or empty if not found
   */
  public Optional<Class<?>> bySimpleName(String simple) {
    return exposed.stream().filter(c -> c.getSimpleName().equalsIgnoreCase(simple)).findFirst();
  }

  /**
   * Resolves an entity by its simple name.
   *
   * @param simple the simple name of the entity
   * @return the entity class if found
   * @throws NotFoundException if the entity is not found
   */
  public Class<?> resolve(String simple) {
    return bySimpleName(simple)
        .orElseThrow(() -> new NotFoundException("Entity not found: " + simple));
  }
}