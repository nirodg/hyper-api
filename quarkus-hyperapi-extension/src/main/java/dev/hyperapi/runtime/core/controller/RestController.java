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

//@Path("/api/{entity}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public abstract class RestController<
        DTO extends BaseDTO,
        MAPPER extends AbstractMapper<DTO, ENTITY>,
        SERVICE extends BaseEntityService<ENTITY, DTO, MAPPER>,
        ENTITY extends BaseEntity> {

//    @Inject
//    EntityRegistry registry;
//    @Inject
//    GenericCrudService service;
//    @Inject
//    EntityConfigProvider configProvider;
//    @Context
//    SecurityContext securityContext;
//    @Context
//    Request request;

    protected final SERVICE service;

    protected RestController(SERVICE service) {
        this.service = service;
    }


    @GET
    public List<DTO> getAll() {
        return service.findAll();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") String id) {
        DTO dto = service.findById(id);
        if (dto == null) {
            throw new NotFoundException("Entity not found");
        }
        return Response.ok(dto).build();
    }

    @POST
    public Response create(DTO dto) {
        return Response.status(Response.Status.CREATED).entity(service.create(dto)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, DTO dto) {
        dto.setGuid(id);
        return Response.ok(service.update(dto)).build();
    }

    @PATCH
    @Path("/{id}")
    @Consumes("application/merge-patch+json")
    public Response patch(@PathParam("id") String id, JsonObject patchJson) {
        DTO patchedDto = service.patch(id, patchJson);
        return Response.ok(patchedDto).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        service.delete(id);
        return Response.noContent().build();
    }

}