package dev.hyperapi.runtime.core.service;

import dev.hyperapi.runtime.core.mapper.DtoMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class GenericCrudService {

    @Inject
    EntityManager em;

    @Inject
    DtoMapper mapper;

    /* ----------  READ  ---------- */
    public List<Map<String, Object>> findAll(Class<?> entityCls) {
        String jpql = "SELECT e FROM " + entityCls.getSimpleName() + " e";
        TypedQuery<?> q = em.createQuery(jpql, entityCls);
        return q.getResultList().stream()
                .map(mapper::toMap)
                .collect(Collectors.toList());
    }

    public Map<String, Object> findById(Class<?> entityCls, Object id) {
        Object entity = em.find(entityCls, id);
        return mapper.toMap(entity);
    }

    /* ----------  CREATE / UPDATE / DELETE  ---------- */
    @Transactional
    public Map<String, Object> create(Class<?> entityCls, Map<String, Object> dto) {
        Object entity = mapper.toEntity(dto, entityCls);
        em.persist(entity);
        return mapper.toMap(entity);
    }

    @Transactional
    public Map<String, Object> update(Class<?> entityCls, Map<String, Object> dto) {
        Object entity = mapper.toEntity(dto, entityCls);
        Object merged = em.merge(entity);
        return mapper.toMap(merged);
    }

    @Transactional
    public void delete(Class<?> entityCls, Object id) {
        Object ref = em.getReference(entityCls, id);
        em.remove(ref);
    }
}
