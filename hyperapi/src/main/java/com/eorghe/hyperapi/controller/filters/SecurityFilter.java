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
package com.eorghe.hyperapi.controller.filters;

import com.eorghe.hyperapi.common.EntityConfigProvider;
import com.eorghe.hyperapi.processor.annotations.Secured;
import com.eorghe.hyperapi.processor.annotations.Security;
import com.eorghe.hyperapi.registry.EntityRegistry;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.jboss.logging.Logger;

/**
 * SecurityFilter is a JAX-RS container request filter that enforces security rules for incoming
 * HTTP requests based on entity-specific configurations.
 *
 * <p>It handles authentication and authorization for API endpoints dynamically
 * by reading security configurations from the entity registry and configuration provider.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class SecurityFilter implements ContainerRequestFilter {

  private static final Logger log = Logger.getLogger(SecurityFilter.class);

  @Inject
  EntityRegistry registry; // for resolving entity classes

  @Inject
  EntityConfigProvider cfgProv; // for reading @HyperResource

  @Context
  UriInfo uriInfo;

  @Context
  SecurityContext securityContext;

  /**
   * Filters incoming requests to enforce security rules.
   *
   * <p>Handles authentication and authorization based on entity-specific security
   * configurations. Requests that fail security checks are aborted with appropriate HTTP error
   * responses.
   *
   * @param ctx the container request context
   * @throws IOException if an I/O error occurs
   */
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
      return; // not a /api/{entity} request
    }

    // 3) Resolve the entity class (case-insensitive)
    Optional<Class<?>> opt = registry.bySimpleName(entityName);
    if (opt.isEmpty()) {
      return; // let the controller return 404
    }
    Class<?> cls = opt.get();

    // 4) Read security configuration
    Security sec = cfgProv.configFor(cls).security();

    // 5) Anonymous by default
    if (!sec.requireAuth() && sec.rolesAllowed().length == 0) {
      return;
    }

    // 6) Enforce authentication if required → 401
    if (sec.requireAuth() && securityContext.getUserPrincipal() == null) {
      log.warnf("Unauthenticated request to %s at %s", entityName, uriInfo.getRequestUri());
      ctx.abortWith(
          buildError(
              Response.Status.UNAUTHORIZED,
              "Unauthorized",
              "Authentication required",
              uriInfo.getPath(),
              true));
      return;
    }

    // 7) Enforce roles if specified → 403
    Set<String> allowed = Set.of(sec.rolesAllowed());
    if (!allowed.isEmpty()) {
      boolean hasRole = allowed.stream().anyMatch(securityContext::isUserInRole);
      if (!hasRole) {
        log.warnf(
            "Forbidden request to %s by user %s",
            entityName, securityContext.getUserPrincipal().getName());
        ctx.abortWith(
            buildError(
                Response.Status.FORBIDDEN,
                "Forbidden",
                "Access denied for entity " + entityName,
                uriInfo.getPath(),
                false));
        return;
      }
    }
    // otherwise: authenticated or anonymous allowed
  }

  /**
   * Builds an error response for aborted requests.
   *
   * @param status         the HTTP status code
   * @param error          the error type
   * @param message        the error message
   * @param path           the request path
   * @param sendAuthHeader whether to include the WWW-Authenticate header
   * @return the constructed error response
   */
  private Response buildError(
      Response.Status status, String error, String message, String path, boolean sendAuthHeader) {
    ApiError payload =
        new ApiError(Instant.now().toString(), status.getStatusCode(), error, message, path);
    Response.ResponseBuilder rb =
        Response.status(status).entity(payload).type(MediaType.APPLICATION_JSON);
    if (sendAuthHeader) {
      rb.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"hyperapi\"");
    }
    return rb.build();
  }
}
