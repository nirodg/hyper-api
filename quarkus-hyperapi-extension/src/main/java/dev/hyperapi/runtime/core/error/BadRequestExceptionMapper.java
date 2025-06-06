package dev.hyperapi.runtime.core.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BadRequestExceptionMapper extends AbstractProblemMapper
    implements jakarta.ws.rs.ext.ExceptionMapper<jakarta.ws.rs.BadRequestException> {
  @Override
  public Response toResponse(jakarta.ws.rs.BadRequestException ex) {
    return buildResponse(
        buildProblem(400, "Bad Request", ex.getMessage(), "https://httpstatuses.com/400"));
  }
}
