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
package com.eorghe.hyperapi.error;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

/**
 * Abstract base class that provides common functionality for building for handling different types
 * of problems in a REST API call.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public class AbstractProblemMapper {

  @Context
  protected jakarta.ws.rs.core.UriInfo uriInfo;

  protected ProblemDetails buildProblem(int status, String title, String detail, String type) {
    return new ProblemDetails(
        status, title, detail, type, uriInfo != null ? uriInfo.getRequestUri().toString() : "");
  }

  protected Response buildResponse(ProblemDetails problem) {
    return Response.status(problem.status).type("application/problem+json").entity(problem).build();
  }
}
