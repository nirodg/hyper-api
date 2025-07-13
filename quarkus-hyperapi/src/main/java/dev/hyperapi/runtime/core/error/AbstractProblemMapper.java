package dev.hyperapi.runtime.core.error;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

public class AbstractProblemMapper {
  @Context protected jakarta.ws.rs.core.UriInfo uriInfo;

  protected ProblemDetails buildProblem(int status, String title, String detail, String type) {
    return new ProblemDetails(
        status, title, detail, type, uriInfo != null ? uriInfo.getRequestUri().toString() : "");
  }

  protected Response buildResponse(ProblemDetails problem) {
    return Response.status(problem.status).type("application/problem+json").entity(problem).build();
  }
}
