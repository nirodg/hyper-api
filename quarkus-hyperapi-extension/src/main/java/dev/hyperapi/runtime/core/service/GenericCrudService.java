package dev.hyperapi.runtime.core.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class GenericCrudService {

    @Inject
    EntityManager em;

    public List<Map<String, Object>> findAll(Class<?> entityClass, int offset, int limit) {
        String queryStr = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        Query query = em.createQuery(queryStr, entityClass);

        if (offset > 0) {
            query.setFirstResult(offset);
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }

        List<?> entities = query.getResultList();

        // Convert entities to Maps to avoid Hibernate serialization issues
        return entities.stream()
                .map(this::entityToMap)
                .collect(Collectors.toList());
    }

    public Map<String, Object> findById(Class<?> entityClass, Long id) {
        Object entity = em.find(entityClass, id);
        if (entity == null) {
            return null;
        }
        return entityToMap(entity);
    }

    @Transactional
    public Map<String, Object> create(Class<?> entityClass, Map<String, Object> data) {
        try {
            Object entity = entityClass.getDeclaredConstructor().newInstance();
            mapToEntity(data, entity);
            em.persist(entity);
            em.flush(); // Ensure ID is generated
            return entityToMap(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create entity", e);
        }
    }

    @Transactional
    public Map<String, Object> update(Class<?> entityClass, Map<String, Object> data) {
        try {
            Long id = extractId(data);
            Object entity = em.find(entityClass, id);
            if (entity == null) {
                throw new RuntimeException("Entity not found for update");
            }

            mapToEntity(data, entity);
            em.merge(entity);
            em.flush();
            return entityToMap(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update entity", e);
        }
    }

    @Transactional
    public void delete(Class<?> entityClass, Long id) {
        Object entity = em.find(entityClass, id);
        if (entity != null) {
            em.remove(entity);
            em.flush();
        }
    }

    /**
     * Convert entity to Map, handling Hibernate-specific issues
     */
    private Map<String, Object> entityToMap(Object entity) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = entity.getClass();

        // Handle Hibernate proxy classes
        if (clazz.getName().contains("$HibernateProxy$")) {
            clazz = clazz.getSuperclass();
        }

        // Get all fields including inherited ones
        List<Field> fields = getAllFields(clazz);

        for (Field field : fields) {
            try {
                // Skip Hibernate internal fields
                if (isHibernateInternalField(field)) {
                    continue;
                }

                field.setAccessible(true);
                Object value = field.get(entity);

                // Handle null values
                if (value == null) {
                    map.put(field.getName(), null);
                    continue;
                }

                // Handle primitive types and strings
                if (isPrimitiveOrWrapper(value.getClass()) || value instanceof String) {
                    map.put(field.getName(), value);
                }
                // Handle dates
                else if (value instanceof java.time.LocalDateTime ||
                        value instanceof java.time.LocalDate ||
                        value instanceof java.util.Date) {
                    map.put(field.getName(), value.toString());
                }
                // Handle collections (basic handling - you might need to expand this)
                else if (value instanceof Collection) {
                    // For now, just put collection size or handle simple cases
                    Collection<?> collection = (Collection<?>) value;
                    if (collection.isEmpty()) {
                        map.put(field.getName(), new ArrayList<>());
                    } else {
                        // You might want to recursively convert collection items
                        map.put(field.getName(), "[Collection of " + collection.size() + " items]");
                    }
                }
                // Handle other entities (basic reference)
                else {
                    // For related entities, just put ID or basic info to avoid circular references
                    try {
                        Field idField = findIdField(value.getClass());
                        if (idField != null) {
                            idField.setAccessible(true);
                            Object idValue = idField.get(value);
                            map.put(field.getName() + "Id", idValue);
                        } else {
                            map.put(field.getName(), value.toString());
                        }
                    } catch (Exception e) {
                        map.put(field.getName(), "[Reference]");
                    }
                }
            } catch (Exception e) {
                // Log the error and continue with other fields
                System.err.println("Error processing field " + field.getName() + ": " + e.getMessage());
            }
        }

        return map;
    }

    /**
     * Convert Map to entity, setting field values
     */
    private void mapToEntity(Map<String, Object> data, Object entity) {
        Class<?> clazz = entity.getClass();
        List<Field> fields = getAllFields(clazz);

        for (Field field : fields) {
            try {
                if (isHibernateInternalField(field) || !data.containsKey(field.getName())) {
                    continue;
                }

                field.setAccessible(true);
                Object value = data.get(field.getName());

                if (value != null) {
                    // Convert value to appropriate type if needed
                    Object convertedValue = convertValue(value, field.getType());
                    field.set(entity, convertedValue);
                }
            } catch (Exception e) {
                System.err.println("Error setting field " + field.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Get all fields including inherited ones
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * Check if field is Hibernate internal field
     */
    private boolean isHibernateInternalField(Field field) {
        String fieldName = field.getName();
        String fieldType = field.getType().getName();

        return fieldName.startsWith("$$_hibernate") ||
                fieldName.equals("handler") ||
                fieldType.contains("hibernate") ||
                fieldType.contains("javassist") ||
                fieldName.equals("entityKey") ||
                fieldName.equals("persister");
    }

    /**
     * Check if class is primitive or wrapper
     */
    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Boolean.class ||
                clazz == Character.class ||
                clazz == Byte.class ||
                clazz == Short.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Float.class ||
                clazz == Double.class;
    }

    /**
     * Find ID field in entity class
     */
    private Field findIdField(Class<?> clazz) {
        List<Field> fields = getAllFields(clazz);
        return fields.stream()
                .filter(f -> f.isAnnotationPresent(jakarta.persistence.Id.class))
                .findFirst()
                .orElse(null);
    }

    /**
     * Convert value to target type
     */
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // Add type conversion logic as needed
        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.valueOf(value.toString());
        }

        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.valueOf(value.toString());
        }

        if (targetType == String.class) {
            return value.toString();
        }

        return value;
    }

    /**
     * Extract ID from data map
     */
    private Long extractId(Map<String, Object> data) {
        Object id = data.get("id");
        if (id instanceof Number) {
            return ((Number) id).longValue();
        }
        if (id instanceof String) {
            return Long.valueOf((String) id);
        }
        throw new RuntimeException("Invalid or missing ID");
    }
}