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
package com.eorghe.hyperapi.llm;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * OllamaRestClient is a REST client interface for interacting with the `/api/generate` endpoint.
 *
 * <p>This interface defines a method for sending prompts to a language model service
 * and receiving responses in JSON format.
 *
 * <p>It uses JAX-RS annotations to specify the HTTP method, path, and media types for
 * request and response handling.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@Path("/api/generate")
public interface OllamaRestClient {

  /**
   * Sends a prompt to the language model service.
   *
   * <p>This method performs a POST request to the `/api/generate` endpoint, sending
   * a JSON payload containing the prompt and receiving a JSON response.
   *
   * @param jsonPayload the JSON payload containing the prompt
   * @return the response from the language model service as a `String`
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  String sendPrompt(ObjectNode jsonPayload);
}