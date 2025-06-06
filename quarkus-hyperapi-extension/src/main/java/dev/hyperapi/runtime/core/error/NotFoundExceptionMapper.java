package dev.hyperapi.runtime.core.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper extends AbstractProblemMapper
    implements jakarta.ws.rs.ext.ExceptionMapper<jakarta.ws.rs.NotFoundException> {
  @Override
  public Response toResponse(jakarta.ws.rs.NotFoundException ex) {
    return buildResponse(
        buildProblem(404, "Resource Not Found", ex.getMessage(), "https://httpstatuses.com/404"));
  }
}
