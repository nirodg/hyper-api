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
package com.eorghe.hyperapi.controller;

import com.eorghe.hyperapi.dto.HyperDto;
import com.eorghe.hyperapi.mapper.AbstractMapper;
import com.eorghe.hyperapi.model.HyperEntity;
import com.eorghe.hyperapi.service.BaseEntityService;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * RestController is an abstract base class for RESTful API controllers.
 *
 * <p>It provides common CRUD operations for entities, including methods for
 * retrieving, creating, updating, patching, and deleting entities.
 *
 * @param <DTO>    the type of the Data Transfer Object
 * @param <MAPPER> the type of the mapper used to convert between DTO and entity
 * @param <ENTITY> the type of the entity
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public abstract class RestController<
    DTO extends HyperDto,
    MAPPER extends AbstractMapper<DTO, ENTITY>,
    ENTITY extends HyperEntity> {

  /**
   * Abstract method to retrieve the service responsible for entity operations.
   *
   * @return the service instance
   */
  protected abstract BaseEntityService<ENTITY, DTO, MAPPER> getService();

  /**
   * Retrieves a paginated list of all entities.
   *
   * @param offset the starting index for pagination (default is 0)
   * @param limit  the maximum number of entities to retrieve (default is 20)
   * @return a list of DTOs representing the entities
   */
  @GET
  public List<DTO> getAll(@QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("20") int limit) {
    return getService().findAll(offset, limit);
  }

  /**
   * Retrieves an entity by its ID.
   *
   * @param id the ID of the entity
   * @return a Response containing the DTO of the entity
   * @throws NotFoundException if the entity is not found
   */
  @GET
  @Path("/{id}")
  public Response getById(@PathParam("id") Long id) {
    DTO dto = getService().findById(id);
    if (dto == null) {
      throw new NotFoundException("Entity not found");
    }
    return Response.ok(dto).build();
  }

  /**
   * Creates a new entity.
   *
   * @param dto the DTO representing the entity to create
   * @return a Response containing the created entity
   */
  @POST
  public Response create(DTO dto) {
    return Response.status(Response.Status.CREATED).entity(getService().create(dto)).build();
  }

  /**
   * Updates an existing entity.
   *
   * @param id  the ID of the entity to update
   * @param dto the DTO containing updated data for the entity
   * @return a Response containing the updated entity
   */
  @PUT
  @Path("/{id}")
  public Response update(@PathParam("id") Long id, DTO dto) {
    dto.setId(id);
    return Response.ok(getService().update(dto)).build();
  }

  /**
   * Partially updates an entity using a JSON merge patch.
   *
   * @param id        the ID of the entity to patch
   * @param patchJson the JSON object containing the patch data
   * @return a Response containing the patched entity
   */
  @PATCH
  @Path("/{id}")
  @Consumes("application/merge-patch+json")
  public Response patch(@PathParam("id") Long id, JsonObject patchJson) {
    DTO patchedDto = getService().patch(id, patchJson);
    return Response.ok(patchedDto).build();
  }

  /**
   * Deletes an entity by its ID.
   *
   * @param id the ID of the entity to delete
   * @return a Response indicating successful deletion
   */
  @DELETE
  @Path("/{id}")
  public Response delete(@PathParam("id") Long id) {
    getService().delete(id);
    return Response.noContent().build();
  }

}