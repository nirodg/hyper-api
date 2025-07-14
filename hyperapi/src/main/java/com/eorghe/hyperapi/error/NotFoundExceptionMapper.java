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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * NotFoundExceptionMapper is a JAX-RS exception mapper that handles
 * `NotFoundException` and converts it into a standardized HTTP response.
 *
 * <p>This class extends `AbstractProblemMapper` to leverage common functionality
 * for building problem details and responses.
 *
 * <p>It is annotated with `@Provider` to register it as a JAX-RS provider.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@Provider
public class NotFoundExceptionMapper extends AbstractProblemMapper
    implements jakarta.ws.rs.ext.ExceptionMapper<jakarta.ws.rs.NotFoundException> {

  /**
   * Converts a `NotFoundException` into a `Response` object.
   *
   * <p>The response includes problem details such as status code, title, detail,
   * and a type URI for further information.
   *
   * @param ex the `NotFoundException` to handle
   * @return a `Response` object with problem details
   */
  @Override
  public Response toResponse(jakarta.ws.rs.NotFoundException ex) {
    return buildResponse(
        buildProblem(404, "Resource Not Found", ex.getMessage(), "https://httpstatuses.com/404"));
  }
}