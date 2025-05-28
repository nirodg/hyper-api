package dev.hyperapi.runtime.core.controller;

import dev.hyperapi.runtime.core.registry.EntityRegistry;
import dev.hyperapi.runtime.core.service.GenericCrudService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/api/{entity}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GenericCrudController {

    @Inject
    EntityRegistry registry;
    @Inject
    GenericCrudService service;

    /* -------- helpers -------- */
    private Class<?> resolve(String simple) {
        return registry.bySimpleName(simple)
                .orElseThrow(() -> new NotFoundException("Entity not found: " + simple));
    }

    /* -------- CRUD endpoints -------- */
    @GET
    public List<Map<String, Object>> getAll(@PathParam("entity") String entity) {
        return service.findAll(resolve(entity));
    }

    @GET
    @Path("/{id}")
    public Map<String, Object> getById(@PathParam("entity") String entity,
            @PathParam("id") Long id) {
        return service.findById(resolve(entity), id);
    }

    @POST
    public Map<String, Object> create(@PathParam("entity") String entity,
            Map<String, Object> body) {
        return service.create(resolve(entity), body);
    }

    @PUT
    @Path("/{id}")
    public Map<String, Object> update(@PathParam("entity") String entity,
            @PathParam("id") Long id,
            Map<String, Object> body) {
        body.put("id", id);
        return service.update(resolve(entity), body);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("entity") String entity,
            @PathParam("id") Long id) {
        service.delete(resolve(entity), id);
    }
}
