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
package com.eorghe.hyperapi;

import com.eorghe.hyperapi.llm.LLMDocsRoute;
import com.eorghe.hyperapi.registry.EntityRegistry;
import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HyperApiStartupHandler is responsible for initializing the HyperAPI extension during application
 * startup.
 *
 * <p>This class observes the Quarkus `StartupEvent` to perform initialization tasks such as:
 * <ul>
 *   <li>Logging the startup of the HyperAPI extension.</li>
 *   <li>Registering entities discovered by the `EntityRegistry`.</li>
 *   <li>Registering routes for the LLM documentation service.</li>
 * </ul>
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@ApplicationScoped
public class HyperApiStartupHandler {

  /**
   * Logger instance for logging startup and initialization details.
   */
  private static final Logger LOG = LoggerFactory.getLogger(HyperApiStartupHandler.class);

  /**
   * The Vert.x router used for registering HTTP routes.
   */
  @Inject
  Router router;

  /**
   * The route handler for LLM documentation services.
   */
  @Inject
  LLMDocsRoute llmDocsRoute;

  /**
   * The registry for managing discovered JPA entities.
   */
  @Inject
  EntityRegistry entityRegistry;

  /**
   * Handles the application startup event.
   *
   * <p>This method is triggered when the Quarkus application starts. It performs the following
   * tasks:
   * <ul>
   *   <li>Logs the successful startup of the HyperAPI extension.</li>
   *   <li>Iterates through all registered entities and logs their names.</li>
   *   <li>Registers the LLM documentation route with the application's router.</li>
   * </ul>
   *
   * @param ev the startup event observed by the application
   */
  void onStart(@Observes StartupEvent ev) {
    LOG.info("HyperAPI extension started successfully!");

    entityRegistry
        .all()
        .forEach(
            entityClass -> {
              LOG.info("Registered entity: {}", entityClass.getName());
              // Optionally, you can initialize services for each entity
              // GenericCrudService service = new GenericCrudService(entityClass);
              // crudService.registerService(entityClass, service);
            });

    llmDocsRoute.register(router);
  }
}