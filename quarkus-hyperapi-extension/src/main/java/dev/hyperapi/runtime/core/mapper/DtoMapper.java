package dev.hyperapi.runtime.core.mapper;

import dev.hyperapi.runtime.annotations.ExposeAPI;
import jakarta.enterprise.context.ApplicationScoped;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Very light reflection mapper: Entity ? Map<String,Object>. Ignores fields
 * declared in @ExposeAPI(mapping.ignore).
 */
@ApplicationScoped
public class DtoMapper {

    /* --------  Entity ? Map  -------- */
    public Map<String, Object> toMap(Object entity) {
        if (entity == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        Class<?> cls = entity.getClass();
        Set<String> ignore = ignored(cls);

        for (Field f : cls.getDeclaredFields()) {
            if (ignore.contains(f.getName())) {
                continue;
            }
            f.setAccessible(true);
            try {
                result.put(f.getName(), f.get(entity));
            } catch (IllegalAccessException ignored1) {
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
        if (!cls.isAnnotationPresent(ExposeAPI.class)) {
            return Set.of();
        }
        return Set.of(cls.getAnnotation(ExposeAPI.class).mapping().ignore());
    }
}
