package dev.hyperapi.runtime.core.service;

import dev.hyperapi.runtime.core.mapper.DtoMapper;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author brage
 * @param <T>
 */
public abstract class BaseEntityService<T> {

    @Inject
    protected EntityManager em;

    @Inject
    protected DtoMapper mapper;

    protected final Class<T> entityClass;

    public BaseEntityService(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /* ----------  GENERIC CRUD METHODS  ---------- */
    public List<Map<String, Object>> findAll() {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        TypedQuery<T> q = em.createQuery(jpql, entityClass);
        return q.getResultList().stream()
                .map(mapper::toMap)
                .collect(Collectors.toList());
    }

    public Map<String, Object> findById(Object id) {
        T entity = em.find(entityClass, id);
        return mapper.toMap(entity);
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> dto) {
        T entity = mapper.toEntity(dto, entityClass);
        em.persist(entity);
        return mapper.toMap(entity);
    }

    @Transactional
    public Map<String, Object> update(Map<String, Object> dto) {
        T entity = mapper.toEntity(dto, entityClass);
        T merged = em.merge(entity);
        return mapper.toMap(merged);
    }

    @Transactional
    public void delete(Object id) {
        T ref = em.getReference(entityClass, id);
        em.remove(ref);
    }

    /* ----------  HELPER METHODS FOR EXTENSIONS  ---------- */
    protected List<T> findByQuery(String jpql, Object... params) {
        TypedQuery<T> query = em.createQuery(jpql, entityClass);
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i + 1, params[i]);
        }
        return query.getResultList();
    }

    protected List<Map<String, Object>> findByQueryAsMap(String jpql, Object... params) {
        return findByQuery(jpql, params).stream()
                .map(mapper::toMap)
                .collect(Collectors.toList());
    }

    protected T findSingleByQuery(String jpql, Object... params) {
        return findByQuery(jpql, params).stream().findFirst().orElse(null);
    }

    protected Map<String, Object> findSingleByQueryAsMap(String jpql, Object... params) {
        T entity = findSingleByQuery(jpql, params);
        return mapper.toMap(entity);
    }
}
