package dev.hyperapi.runtime.core.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ForbiddenExceptionMapper extends AbstractProblemMapper
    implements jakarta.ws.rs.ext.ExceptionMapper<jakarta.ws.rs.ForbiddenException> {
  @Override
  public Response toResponse(jakarta.ws.rs.ForbiddenException ex) {
    return buildResponse(
        buildProblem(403, "Forbidden", ex.getMessage(), "https://httpstatuses.com/403"));
  }
}
