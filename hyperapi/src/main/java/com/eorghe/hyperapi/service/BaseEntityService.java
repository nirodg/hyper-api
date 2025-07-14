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
package com.eorghe.hyperapi.service;

import com.eorghe.hyperapi.dto.HyperDto;
import com.eorghe.hyperapi.events.EntityEvent;
import com.eorghe.hyperapi.mapper.AbstractMapper;
import com.eorghe.hyperapi.model.HyperEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonMergePatch;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;

/**
 * BaseEntityService provides a generic CRUD service for managing JPA entities and their DTOs.
 *
 * <p>This abstract class supports basic CRUD operations, PATCH-style updates, and event firing
 * for entities. It uses a mapper to convert between entities and DTOs, and a repository for
 * persistence operations.
 *
 * @param <ENTITY> the type of the JPA entity
 * @param <DTO>    the type of the DTO
 * @param <MAPPER> the type of the mapper used for entity-DTO conversion
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public abstract class BaseEntityService<
    ENTITY extends HyperEntity, DTO extends HyperDto, MAPPER extends AbstractMapper<DTO, ENTITY>> {

  /**
   * The class type of the DTO, used for JSON deserialization.
   */
  private final Class<DTO> dtoClass;

  /**
   * Constructs a BaseEntityService with the specified DTO class type.
   *
   * @param dtoClass the class type of the DTO
   */
  protected BaseEntityService(Class<DTO> dtoClass) {
    this.dtoClass = dtoClass;
  }

  /**
   * The mapper used for converting between entities and DTOs.
   */
  @Inject
  protected MAPPER mapper;

  /**
   * The JSON-B instance used for JSON serialization and deserialization.
   */
  @Inject
  Jsonb jsonb;

  /**
   * The CDI event used for firing entity-related events.
   */
  @Inject
  jakarta.enterprise.event.Event<EntityEvent<ENTITY>> event;

  /**
   * Returns the repository for managing the entity.
   *
   * @return the repository instance
   */
  protected abstract PanacheRepositoryBase<ENTITY, Long> getRepository();

  /**
   * Retrieves a paginated list of DTOs.
   *
   * @param offset the starting index of the page
   * @param limit  the maximum number of items in the page
   * @return a list of DTOs
   */
  public List<DTO> findAll(int offset, int limit) {
    return mapper.toList(
        getRepository().findAll()
            .page(offset / limit, limit)
            .list()
    );
  }

  /**
   * Finds a DTO by its ID.
   *
   * @param id the ID of the entity
   * @return the corresponding DTO, or null if not found
   */
  public DTO findById(Long id) {
    ENTITY entity = getRepository().findById(id);
    return entity != null ? mapper.toDto(entity) : null;
  }

  /**
   * Creates a new entity from the given DTO.
   *
   * @param dto the DTO representing the entity to create
   * @return the created DTO
   */
  @Transactional
  public DTO create(DTO dto) {
    ENTITY entity = mapper.toEntity(dto);
    getRepository().persist(entity);
    return mapper.toDto(entity);
  }

  /**
   * Updates an existing entity with the given DTO.
   *
   * @param dto the DTO representing the updated entity
   * @return the updated DTO
   */
  @Transactional
  public DTO update(DTO dto) {
    ENTITY entity = mapper.toEntity(dto);
    ENTITY merged = getRepository().getEntityManager().merge(entity);
    return mapper.toDto(merged);
  }

  /**
   * Deletes an entity by its ID.
   *
   * @param id the ID of the entity to delete
   */
  @Transactional
  public void delete(Long id) {
    getRepository().deleteById(id);
  }

  /**
   * Applies a JSON Merge Patch to an entity and updates it.
   *
   * @param id        the ID of the entity to patch
   * @param patchJson the JSON object representing the patch
   * @return the patched DTO
   * @throws NotFoundException if the entity is not found
   */
  @Transactional
  public DTO patch(Long id, JsonObject patchJson) {
    DTO existingDto = findById(id);
    if (existingDto == null) {
      throw new NotFoundException("Entity not found");
    }

    // Serialize full DTO into JsonObject
    JsonObject existingJson = jsonb.fromJson(jsonb.toJson(existingDto), JsonObject.class);

    // Merge patch
    JsonMergePatch mergePatch = Json.createMergePatch(patchJson);
    JsonValue patchedValue = mergePatch.apply(existingJson);

    JsonObject mergedJson = patchedValue.asJsonObject();

    DTO patchedDto = jsonToDto(mergedJson);
    // Strip immutable fields after patch
    patchedDto.setCreatedOn(existingDto.getCreatedOn());
    patchedDto.setCreatedBy(existingDto.getCreatedBy());
    patchedDto.setId(id); // Ensure ID is preserved

    return update(patchedDto);
  }

  /**
   * Converts a JSON object to a DTO.
   *
   * @param json the JSON object
   * @return the corresponding DTO
   */
  private DTO jsonToDto(JsonObject json) {
    return jsonb.fromJson(json.toString(), dtoClass);
  }

  /**
   * Fires an entity-related event.
   *
   * @param type   the type of the event
   * @param entity the entity associated with the event
   */
  protected void fireEvent(EntityEvent.Type type, ENTITY entity) {
    if (event != null && entity != null) {
      event.fire(new EntityEvent<>(type, entity));
    }
  }
}