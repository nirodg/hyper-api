/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.hyperapi.runtime.core;

import dev.hyperapi.runtime.core.llm.LLMDocsRoute;
import dev.hyperapi.runtime.core.mapper.DtoMapper;
import dev.hyperapi.runtime.core.registry.EntityRegistry;
import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class HyperApiStartupHandler {

  private static final Logger LOG = LoggerFactory.getLogger(HyperApiStartupHandler.class);

  @Inject
  Router router;

  @Inject
  LLMDocsRoute llmDocsRoute;

  @Inject
  DtoMapper dtoMapper;

  @Inject
  EntityRegistry entityRegistry;

  void onStart(@Observes StartupEvent ev) {
    LOG.info("HyperAPI extension started successfully!");
    LOG.info("DtoMapper instance: {}", dtoMapper != null ? "OK" : "NULL");

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
