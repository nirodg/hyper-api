package dev.hyperapi.runtime.core.service;

import dev.hyperapi.runtime.core.dto.BaseDTO;
import dev.hyperapi.runtime.core.events.EntityEvent;
import dev.hyperapi.runtime.core.mapper.AbstractMapper;
import dev.hyperapi.runtime.core.model.BaseEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonMergePatch;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;

/**
 * Base CRUD service with DTO and PATCH-style support.
 *
 * @param <ENTITY> JPA Entity type
 * @param <DTO> DTO type
 * @param <MAPPER> Mapper type
 */
public abstract class BaseEntityService<
    ENTITY extends BaseEntity, DTO extends BaseDTO, Id, MAPPER extends AbstractMapper<DTO, ENTITY>> {

  private final Class<DTO> dtoClass;

  protected BaseEntityService(Class<DTO> dtoClass) {
    this.dtoClass = dtoClass;
  }

  @Inject protected MAPPER mapper;

  @Inject Jsonb jsonb;

  @Inject jakarta.enterprise.event.Event<EntityEvent<ENTITY>> event;

  protected abstract PanacheRepositoryBase<ENTITY, Id> getRepository();

  public List<DTO> findAll(int offset, int limit) {
    return mapper.toList(
            getRepository().findAll()
                    .page(offset / limit, limit)
                    .list()
    );
  }

  public DTO findById(Id id) {
    ENTITY entity = getRepository().findById(id);
    return entity != null ? mapper.toDto(entity) : null;
  }

  @Transactional
  public DTO create(DTO dto) {
    ENTITY entity = mapper.toEntity(dto);
    getRepository().persist(entity);
    return mapper.toDto(entity);
  }

  @Transactional
  public DTO update(DTO dto) {
    ENTITY entity = mapper.toEntity(dto);
    ENTITY merged = getRepository().getEntityManager().merge(entity);
    return mapper.toDto(merged);
  }

  @Transactional
  public void delete(Id id) {
    getRepository().deleteById(id);
  }

  @Transactional
  public DTO patch(Id id, JsonObject patchJson) {
    DTO existingDto = findById(id);
    if (existingDto == null) {
      throw new NotFoundException("Entity not found");
    }

    // Convert DTO to JSON
    JsonObject existingJson =
        Json.createObjectBuilder()
            .add(
                "id",
                ((BaseDTO) existingDto).getId().toString()) // Extend this to include all fields as needed
            .build();

    // Apply patch
    JsonMergePatch mergePatch = Json.createMergePatch(patchJson);
    JsonValue patchedValue = mergePatch.apply(existingJson);
    JsonObject patchedJson = patchedValue.asJsonObject();

    // Deserialize into new DTO (or manually hydrate)
    DTO patchedDto = jsonToDto(patchedJson);
    patchedDto.setId(id); // ensure ID is preserved

    return update(patchedDto);
  }

  private DTO jsonToDto(JsonObject json) {
    return jsonb.fromJson(json.toString(), dtoClass);
  }

  protected void fireEvent(EntityEvent.Type type, ENTITY entity) {
    if (event != null && entity != null) {
      event.fire(new EntityEvent<>(type, entity));
    }
  }
}
