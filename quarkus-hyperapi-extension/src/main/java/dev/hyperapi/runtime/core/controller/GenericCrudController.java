package dev.hyperapi.runtime.core.controller;

import dev.hyperapi.runtime.annotations.RestService;
import dev.hyperapi.runtime.core.common.EntityConfigProvider;
import dev.hyperapi.runtime.core.registry.EntityRegistry;
import dev.hyperapi.runtime.core.service.GenericCrudService;
import jakarta.inject.Inject;
import jakarta.json.*;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
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
    @Context
    Request request;

    /* -------- CRUD endpoints -------- */
    @GET
    public Response getAll(
            @PathParam("entity") String entityName,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("-1") int size
    ) {
        Class<?> cls = resolveAndCheck(entityName);
        RestService cfg = configProvider.configFor(cls);
        int limit = size < 0 ? cfg.pageable().limit() : Math.min(size, cfg.pageable().maxLimit());
        int offset = page * limit;
        return Response.ok(service.findAll(cls, offset, limit)).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("entity") String entity,
                            @PathParam("id") Long id) {
        Class<?> cls = resolveAndCheck(entity);
        Map<String, Object> result = service.findById(cls, id);
        if (result == null) {
            throw new NotFoundException("Entity not found");
        }
        return Response.ok(result).build();
    }

    @POST
    public Response create(@PathParam("entity") String entity,
                           Map<String, Object> body) {
        Class<?> cls = resolveAndCheck(entity);
        Map<String, Object> created = service.create(cls, body);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("entity") String entity,
                           @PathParam("id") Long id,
                           Map<String, Object> body) {
        Class<?> cls = resolveAndCheck(entity);
        body.put("id", id);
        Map<String, Object> updated = service.update(cls, body);
        return Response.ok(updated).build();
    }

    @PATCH
    @Path("/{id}")
    @Consumes("application/merge-patch+json")
    public Response patch(@PathParam("entity") String entity,
                          @PathParam("id") Long id,
                          JsonObject patchJson) {
        Class<?> cls = resolveAndCheck(entity);

        // 1. Retrieve existing entity as JsonObject
        Map<String, Object> existingMap = service.findById(cls, id);
        if (existingMap == null) {
            throw new NotFoundException("Entity not found");
        }
        JsonObject existingJson = Json.createObjectBuilder(existingMap).build();

        // 2. Apply JSON Merge Patch
        JsonMergePatch mergePatch;
        try {
            mergePatch = Json.createMergePatch(patchJson);
        } catch (ClassCastException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid JSON Merge Patch format")).build();
        }

        JsonValue patchedValue;
        try {
            patchedValue = mergePatch.apply(existingJson);
        } catch (JsonException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Merge patch application failed"))
                    .build();
        }

        // 3. Convert patched JsonObject back to Map
        JsonObject patchedJson = patchedValue.asJsonObject();
        Map<String, Object> patchedMap = jsonObjectToMap(patchedJson);

        // 4. Validate patched fields against ignore list
        RestService cfg = configProvider.configFor(cls);
        String[] excludeFields = cfg.patchable().exclude();
        String validationError = validatePatch(patchJson, excludeFields);
        if (validationError != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", validationError)).build();
        }

        // 5. Persist updated entity
        patchedMap.put("id", id);
        Map<String, Object> updated = service.update(cls, patchedMap);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("entity") String entity,
                           @PathParam("id") Long id) {
        Class<?> cls = resolveAndCheck(entity);
        service.delete(cls, id);
        return Response.noContent().build();
    }

    /* -------- helpers -------- */
    private Class<?> resolveAndCheck(String entityName) {
        Class<?> cls = registry.resolve(entityName);
        RestService cfg = configProvider.configFor(cls);
        checkDisabled(cfg);
        checkRoles(cls);
        return cls;
    }

    private void checkRoles(String className) {
        checkRoles(registry.resolve(className));
    }

    private void checkRoles(Class<?> entityClass) {
        RestService cfg = configProvider.configFor(entityClass);
        String[] allowed = cfg.security().rolesAllowed();
        if (allowed.length > 0) {
            boolean has = Stream.of(allowed).anyMatch(securityContext::isUserInRole);
            if (!has) {
                throw new ForbiddenException("Not allowed");
            }
        }
    }

    private void checkDisabled(RestService cfg) {
        if(cfg == null) {
            return; // No configuration available for this entity
        }
        if(cfg.disabledFor() == null) {
            return; // No disabled methods configured
        }
        jakarta.ws.rs.HttpMethod[] methods = cfg.disabledFor().disabledFor();
        if (methods != null) {
            String currentMethod = request.getMethod();
            for (jakarta.ws.rs.HttpMethod m : methods) {
                if (m.value().equalsIgnoreCase(currentMethod)) {
                    throw new NotFoundException("Requests disabled for this HTTP method");
                }
            }
        }
    }

    private Map<String, Object> jsonObjectToMap(JsonObject json) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, JsonValue> entry : json.entrySet()) {
            JsonValue.ValueType type = entry.getValue().getValueType();
            switch (type) {
                case OBJECT:
                    map.put(entry.getKey(), jsonObjectToMap(json.getJsonObject(entry.getKey())));
                    break;
                case ARRAY:
                    // Depending on requirements, handle arrays (e.g., List<Object>)
                    break;
                case STRING:
                    map.put(entry.getKey(), json.getString(entry.getKey()));
                    break;
                case NUMBER:
                    map.put(entry.getKey(), json.getJsonNumber(entry.getKey()).numberValue());
                    break;
                case TRUE:
                case FALSE:
                    map.put(entry.getKey(), json.getBoolean(entry.getKey()));
                    break;
                case NULL:
                    map.put(entry.getKey(), null);
                    break;
            }
        }
        return map;
    }

    private String validatePatch(JsonObject patchJson, String[] ignoreFields) {
        for (String key : patchJson.keySet()) {
            if (Arrays.asList(ignoreFields).contains(key)) {
                return "Field '" + key + "' is not updatable.";
            }
        }
        return null;
    }
}