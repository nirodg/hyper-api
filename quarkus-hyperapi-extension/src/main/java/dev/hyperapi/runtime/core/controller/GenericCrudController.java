package dev.hyperapi.runtime.core.controller;

import dev.hyperapi.runtime.annotations.ExposeAPI;
import dev.hyperapi.runtime.core.common.EntityConfigProvider;
import dev.hyperapi.runtime.core.registry.EntityRegistry;
import dev.hyperapi.runtime.core.service.GenericCrudService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Path("/api/{entity}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GenericCrudController {

    @Inject
    EntityRegistry registry;
    @Inject
    GenericCrudService service;
    @Inject
    EntityConfigProvider configProvider;
    @Context
    SecurityContext securityContext;

    /* -------- CRUD endpoints -------- */
    @GET
    public List<Map<String, Object>> getAll(
            @PathParam("entity") String entityName,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("-1") int size
    ) {
        Class<?> cls = registry.resolve(entityName);
        checkRoles(cls);
        ExposeAPI cfg = configProvider.configFor(cls);
        // apply defaults & max
        int limit = size < 0 ? cfg.pageable().limit() : Math.min(size, cfg.pageable().maxLimit());
        int offset = page * limit;
        return service.findAll(cls, offset, limit);
    }

    @GET
    @Path("/{id}")
    public Map<String, Object> getById(@PathParam("entity") String entity,
            @PathParam("id") Long id) {
        checkRoles(entity);
        return service.findById(registry.resolve(entity), id);
    }

    @POST
    public Map<String, Object> create(@PathParam("entity") String entity,
            Map<String, Object> body) {
        checkRoles(entity);
        return service.create(registry.resolve(entity), body);
    }

    @PUT
    @Path("/{id}")
    public Map<String, Object> update(@PathParam("entity") String entity,
            @PathParam("id") Long id,
            Map<String, Object> body) {

        body.put("id", id);
        return service.update(registry.resolve(entity), body);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("entity") String entity,
            @PathParam("id") Long id) {
        service.delete(registry.resolve(entity), id);
    }

    /* -------- helpers -------- */
    private void checkRoles(String className) {
        checkRoles(registry.resolve(className));
    }

    private void checkRoles(Class<?> entityClass) {
        ExposeAPI cfg = configProvider.configFor(entityClass);
        String[] allowed = cfg.security().rolesAllowed();
        if (allowed.length > 0) {
            boolean has = Stream.of(allowed).anyMatch(securityContext::isUserInRole);
            if (!has) {
                throw new ForbiddenException("Not allowed");
            }
        }
    }
}
