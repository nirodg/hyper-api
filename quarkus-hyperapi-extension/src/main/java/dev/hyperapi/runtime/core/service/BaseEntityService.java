package dev.hyperapi.runtime.core.service;

import dev.hyperapi.runtime.core.dto.BaseDTO;
import dev.hyperapi.runtime.core.mapper.AbstractMapper;
import dev.hyperapi.runtime.core.model.BaseEntity;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author brage
 */
public abstract class BaseEntityService<
        ENTITY extends BaseEntity,
        DTO extends BaseDTO,
        MAPPER extends AbstractMapper<DTO, ENTITY>> {

    private final Class<ENTITY> entityClass;

    protected BaseEntityService(Class<ENTITY> entityClass) {
        this.entityClass = entityClass;
    }

    @Inject
    protected EntityManager em;

    @Inject
    protected MAPPER mapper;

    public List<DTO> findAll() {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        TypedQuery<ENTITY> query = em.createQuery(jpql, entityClass);
        List<ENTITY> entities = query.getResultList();

        List<DTO> dtos = new ArrayList<>();
        for (ENTITY entity : entities) {
            dtos.add(mapper.toDto(entity));
        }
        return dtos;
    }

    public Map<String, Object> findById(Object id) {
        ENTITY entity = em.find(entityClass, id);
        return entity != null ? mapper.toMap(entity) : null;
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> dto) {
        ENTITY entity = mapper.toEntity(dto, entityClass);
        em.persist(entity);
        return mapper.toMap(entity);
    }

    @Transactional
    public Map<String, Object> update(Map<String, Object> dto) {
        ENTITY entity = mapper.toEntity(dto, entityClass);
        ENTITY merged = em.merge(entity);
        return mapper.toMap(merged);
    }

    @Transactional
    public void delete(Object id) {
        ENTITY ref = em.getReference(entityClass, id);
        em.remove(ref);
    }

}
