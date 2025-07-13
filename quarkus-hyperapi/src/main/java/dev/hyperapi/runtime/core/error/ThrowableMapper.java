package dev.hyperapi.runtime.core.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ThrowableMapper extends AbstractProblemMapper
    implements jakarta.ws.rs.ext.ExceptionMapper<Throwable> {
  @Override
  public Response toResponse(Throwable ex) {
    return buildResponse(
        buildProblem(
            500,
            "Unexpected Error",
            ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
            "https://httpstatuses.com/500"));
  }
}
