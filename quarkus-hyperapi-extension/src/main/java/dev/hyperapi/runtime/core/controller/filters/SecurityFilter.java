package dev.hyperapi.runtime.core.controller.filters;

import dev.hyperapi.runtime.annotations.ExposeAPI;
import dev.hyperapi.runtime.annotations.Secured;
import dev.hyperapi.runtime.core.common.EntityConfigProvider;
import dev.hyperapi.runtime.core.registry.EntityRegistry;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.jboss.logging.Logger;

@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class SecurityFilter implements ContainerRequestFilter {

    private static final Logger log = Logger.getLogger(SecurityFilter.class);

    @Inject
    EntityRegistry registry;                 // for resolving entity classes

    @Inject
    EntityConfigProvider cfgProv;            // for reading @ExposeAPI

    @Context
    UriInfo uriInfo;

    @Context
    SecurityContext securityContext;

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        // 1) Skip OPTIONS & HEAD
        String method = ctx.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            return;
        }

        // 2) Extract {entity} from path parameters
        String entityName = uriInfo.getPathParameters().getFirst("entity");
        if (entityName == null) {
            return;  // not a /api/{entity} request
        }

        // 3) Resolve the entity class (case-insensitive)
        Optional<Class<?>> opt = registry.bySimpleName(entityName);
        if (opt.isEmpty()) {
            return;  // let the controller return 404
        }
        Class<?> cls = opt.get();

        // 4) Read security configuration
        ExposeAPI.Security sec = cfgProv.configFor(cls).security();

        // 5) Anonymous by default
        if (!sec.requireAuth() && sec.rolesAllowed().length == 0) {
            return;
        }

        // 6) Enforce authentication if required → 401
        if (sec.requireAuth() && securityContext.getUserPrincipal() == null) {
            log.warnf("Unauthenticated request to %s at %s",
                    entityName, uriInfo.getRequestUri());
            ctx.abortWith(buildError(
                    Response.Status.UNAUTHORIZED,
                    "Unauthorized",
                    "Authentication required",
                    uriInfo.getPath(),
                    true
            ));
            return;
        }

        // 7) Enforce roles if specified → 403
        Set<String> allowed = Set.of(sec.rolesAllowed());
        if (!allowed.isEmpty()) {
            boolean hasRole = allowed.stream()
                    .anyMatch(securityContext::isUserInRole);
            if (!hasRole) {
                log.warnf("Forbidden request to %s by user %s",
                        entityName,
                        securityContext.getUserPrincipal().getName());
                ctx.abortWith(buildError(
                        Response.Status.FORBIDDEN,
                        "Forbidden",
                        "Access denied for entity " + entityName,
                        uriInfo.getPath(),
                        false
                ));
                return;
            }
        }
        // otherwise: authenticated or anonymous allowed
    }

    private Response buildError(Response.Status status,
            String error,
            String message,
            String path,
            boolean sendAuthHeader) {
        ApiError payload = new ApiError(
                Instant.now().toString(),
                status.getStatusCode(),
                error,
                message,
                path
        );
        Response.ResponseBuilder rb = Response.status(status)
                .entity(payload)
                .type(MediaType.APPLICATION_JSON);
        if (sendAuthHeader) {
            rb.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"hyperapi\"");
        }
        return rb.build();
    }
}
