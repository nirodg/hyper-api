package dev.hyperapi.runtime.core.mapper;

import dev.hyperapi.runtime.core.processor.annotations.HyperResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import org.hibernate.proxy.HibernateProxy;

/**
 * Very light reflection mapper: Entity ? Map<String,Object>. Ignores fields declared
 * in @HyperResource(mapping.ignore).
 */
@ApplicationScoped
public class DtoMapper {

  /* --------  Entity ? Map  -------- */
  public Map<String, Object> toMap(Object input) {
    if (input == null) {
      return Collections.emptyMap();
    }

    // 1) Unwrap proxies
    Object entity = unwrap(input);
    Class<?> cls = entity.getClass();

    // 2) Must be a JPA @Entity
    if (!cls.isAnnotationPresent(Entity.class)) {
      throw new IllegalArgumentException("Cannot map non-entity class: " + cls.getName());
    }
    // 3) Build the DTO map
    Set<String> ignore = ignored(cls);
    Map<String, Object> result = new LinkedHashMap<>();
    for (Field field : cls.getDeclaredFields()) {
      // skip static, skip ignored
      if (Modifier.isStatic(field.getModifiers()) || ignore.contains(field.getName())) {
        continue;
      }
      field.setAccessible(true);
      try {
        result.put(field.getName(), field.get(entity));
      } catch (IllegalAccessException e) {
        // ignore unreadable fields
      }
    }
    return result;
  }

  /* --------  Map ? Entity  -------- */
  @SuppressWarnings("unchecked")
  public <T> T toEntity(Map<String, Object> map, Class<T> type) {
    try {
      T instance = type.getDeclaredConstructor().newInstance();
      Set<String> ignore = ignored(type);

      for (Map.Entry<String, Object> e : map.entrySet()) {
        if (ignore.contains(e.getKey())) {
          continue;
        }
        Field f = type.getDeclaredField(e.getKey());
        f.setAccessible(true);
        f.set(instance, e.getValue());
      }
      return instance;
    } catch (Exception ex) {
      throw new IllegalStateException("Cannot map DTO to " + type, ex);
    }
  }

  /* --------  helper  -------- */
  private static Set<String> ignored(Class<?> cls) {
    if (!cls.isAnnotationPresent(HyperResource.class)) {
      return Set.of();
    }
    return Set.of(cls.getAnnotation(HyperResource.class).mapping().ignore());
  }

  // Unwrap HibernateProxy or any CGLIB-derived subclass (proxy)
  private static Object unwrap(Object input) {
    // Hibernate proxy?
    if (input instanceof HibernateProxy hp) {
      return hp.getHibernateLazyInitializer().getImplementation();
    }
    // Quarkus/CDI or CGLIB proxy often has $$ in the class name
    Class<?> cls = input.getClass();
    if (cls.getName().contains("$$") && cls.getSuperclass() != null) {
      return cls.getSuperclass().cast(input);
    }
    return input;
  }
}
