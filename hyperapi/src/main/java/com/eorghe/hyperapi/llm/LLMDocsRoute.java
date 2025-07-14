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
package com.eorghe.hyperapi.llm;

import com.eorghe.hyperapi.processor.annotations.Cache;
import com.eorghe.hyperapi.processor.annotations.Events;
import com.eorghe.hyperapi.processor.annotations.HyperResource;
import com.eorghe.hyperapi.processor.annotations.Pageable;
import com.eorghe.hyperapi.processor.annotations.Security;
import com.eorghe.hyperapi.registry.EntityRegistry;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LLMDocsRoute is a class responsible for registering and handling the `/docs/ai` route.
 *
 * <p>This route generates OpenAPI documentation for entities registered in the system
 * using a language model service.
 *
 * <p>The class is annotated with `@ApplicationScoped` to indicate that it is a CDI bean
 * with application scope.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@ApplicationScoped
public class LLMDocsRoute {

  /**
   * Logger instance for logging messages related to the route.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LLMDocsRoute.class);

  /**
   * Service for generating documentation using a language model.
   */
  @Inject
  LLMDocService docService;

  /**
   * Registry containing all entities in the system.
   */
  @Inject
  EntityRegistry registry;

  /**
   * Configuration instance for accessing application properties.
   */
  @Inject
  SmallRyeConfig config;

  /**
   * Registers the `/docs/ai` route with the provided Vert.x router.
   *
   * <p>If the route is disabled via configuration, it logs a message and does not register the
   * route.
   *
   * @param router the Vert.x router to register the route with
   */
  public void register(io.vertx.ext.web.Router router) {
    if (!config.getOptionalValue("hyperapi.llm.docs.enabled", Boolean.class).orElse(false)) {
      LOG.info("LLM /docs/ai route is disabled via configuration.");
      return;
    }
    router.route("/docs/ai").handler(BodyHandler.create());
    router.get("/docs/ai").handler(this::generateOpenApiDocs);
    LOG.info("LLM /docs/ai route registered.");
  }

  /**
   * Handles the generation of OpenAPI documentation for all registered entities.
   *
   * <p>This method retrieves all entities from the registry, generates OpenAPI specifications
   * for each entity using the language model service, and sends the combined documentation as a
   * response.
   *
   * @param ctx the routing context for the HTTP request
   */
  private void generateOpenApiDocs(RoutingContext ctx) {

    // Retrieve all registered entities
    Set<Class<?>> entities = registry.all();

    // Generate documentation tasks for each entity
    List<Uni<String>> tasks = entities.stream()
        .map(entity -> {

          // Build the OpenAPI specification for the entity
          StringBuilder entitySpec = new StringBuilder("Entity: ")
              .append(entity.getSimpleName()).append("\n")
              .append("Base path: /api/").append(entity.getSimpleName().toLowerCase()).append("\n")
              .append("CRUD Operations:\n");
          HyperResource metadata = entity.getAnnotation(HyperResource.class);
          String path = metadata != null && !metadata.path().isEmpty()
              ? metadata.path()
              : "/api/" + entity.getSimpleName().toLowerCase();

          Pageable paging = metadata != null ? metadata.pageable() : null;
          Security security = metadata != null ? metadata.security() : null;
          Events events = metadata != null ? metadata.events() : null;
          Cache cache = metadata != null ? metadata.cache() : null;

          entitySpec.append("Entity: ").append(entity.getSimpleName()).append("\n")
              .append("Base path: ").append(path).append("\n")
              .append("CRUD Operations:\n");

          // Paging
          if (paging != null && paging.limit() > 0) {
            entitySpec.append("- GET ").append(path)
                .append(" → paginated (params: ?page, ?size; default size: ")
                .append(paging.limit()).append(", max: ").append(paging.maxLimit()).append(")\n");
          } else {
            entitySpec.append("- GET ").append(path).append("\n");
          }

          // Standard CRUD
          entitySpec.append("- GET ").append(path).append("/{id}\n")
              .append("- POST ").append(path).append("\n")
              .append("- PUT ").append(path).append("/{id}\n")
              .append("- DELETE ").append(path).append("/{id}\n");

          // Fields
          entitySpec.append("Fields:\n");
          for (Field field : entity.getDeclaredFields()) {
            if (!field.getName().startsWith("$$")) {
              entitySpec.append("  - ").append(field.getName())
                  .append(" (type: ").append(field.getType().getSimpleName()).append(")\n");
            }
          }
          // Security
          if (security != null) {
            entitySpec.append("Security:\n")
                .append("  - Auth Required: ").append(security.requireAuth()).append("\n");
          }
          // Events
          if (events != null) {
            entitySpec.append("Event Broadcasting:\n")
                .append("  - onCreate: ").append(events.onCreate()).append("\n")
                .append("  - onUpdate: ").append(events.onUpdate()).append("\n")
                .append("  - onDelete: ").append(events.onDelete()).append("\n");
          }
          // Cache
          if (cache != null && cache.enabled()) {
            entitySpec.append("Cache:\n")
                .append("  - Enabled: true\n")
                .append("  - TTL: ").append(cache.ttlSeconds()).append(" seconds\n");
          }

          return docService.generateOpenApiDoc(entitySpec.toString());
        })
        .toList();

    // Combine and send the generated documentation
    Uni.join()
        .all(tasks)
        .andCollectFailures()

        .onItem().transform(results ->
            results.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n\n---\n\n"))
        )
        .subscribe().with(
            docs -> ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(docs),
            err -> {
              LOG.error("Failed to generate LLM docs", err);
              ctx.response().setStatusCode(500).end("[ERROR] Failed to generate documentation");
            }
        );

  }

}