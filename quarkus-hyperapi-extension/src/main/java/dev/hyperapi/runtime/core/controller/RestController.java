package dev.hyperapi.runtime.core.controller;

import dev.hyperapi.runtime.core.dto.BaseDTO;
import dev.hyperapi.runtime.core.mapper.AbstractMapper;
import dev.hyperapi.runtime.core.model.BaseEntity;
import dev.hyperapi.runtime.core.service.BaseEntityService;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public abstract class RestController<
        DTO extends BaseDTO,
        MAPPER extends AbstractMapper<DTO, ENTITY>,
        ENTITY extends BaseEntity> {

    protected abstract BaseEntityService<ENTITY, DTO, MAPPER> getService();

    @GET
    public List<DTO> getAll() {
        return getService().findAll();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") String id) {
        DTO dto = getService().findById(id);
        if (dto == null) {
            throw new NotFoundException("Entity not found");
        }
        return Response.ok(dto).build();
    }

    @POST
    public Response create(DTO dto) {
        return Response.status(Response.Status.CREATED).entity(getService().create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, DTO dto) {
        dto.setGuid(id);
        return Response.ok(getService().update(dto)).build();
    }

    @PATCH
    @Path("/{id}")
    @Consumes("application/merge-patch+json")
    public Response patch(@PathParam("id") String id, JsonObject patchJson) {
        DTO patchedDto = getService().patch(id, patchJson);
        return Response.ok(patchedDto).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        getService().delete(id);
        return Response.noContent().build();
    }

}