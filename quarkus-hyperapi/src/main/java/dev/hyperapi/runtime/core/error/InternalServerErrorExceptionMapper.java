package dev.hyperapi.runtime.core.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InternalServerErrorExceptionMapper extends AbstractProblemMapper
    implements jakarta.ws.rs.ext.ExceptionMapper<jakarta.ws.rs.InternalServerErrorException> {
  @Override
  public Response toResponse(jakarta.ws.rs.InternalServerErrorException ex) {
    return buildResponse(
        buildProblem(
            500, "Internal Server Error", ex.getMessage(), "https://httpstatuses.com/500"));
  }
}
