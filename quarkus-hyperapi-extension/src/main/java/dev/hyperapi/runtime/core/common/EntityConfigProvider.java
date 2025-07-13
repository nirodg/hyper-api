/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.hyperapi.runtime.core.common;

import dev.hyperapi.runtime.core.processor.annotations.HyperResource;
import dev.hyperapi.runtime.core.registry.EntityRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * S small helper bean that, for each exposed entity, reads and caches its @ExposeAPI settings
 *
 * @author brage
 */
@ApplicationScoped
public class EntityConfigProvider {

  @Inject private EntityRegistry registry;

  private final Map<Class<?>, HyperResource> config = new ConcurrentHashMap<>();

  public HyperResource configFor(Class<?> cls) {
    return config.computeIfAbsent(
        cls,
        c -> {
          HyperResource ann = c.getAnnotation(HyperResource.class);
          if (ann == null) {
            throw new IllegalStateException("Entity not @ExposeAPI: " + c);
          }
          return ann;
        });
  }

  public Class<?> resolve(String simple) {
    return registry
        .bySimpleName(simple)
        .orElseThrow(() -> new NotFoundException("Entity not found: " + simple));
  }
}
