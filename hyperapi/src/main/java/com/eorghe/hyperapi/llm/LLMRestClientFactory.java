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

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import java.net.URI;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * LLMRestClientFactory is a factory class for creating instances of `OllamaRestClient`.
 *
 * <p>This class uses Quarkus' reactive REST client builder to produce a client
 * configured with a base URL retrieved from the application's configuration.
 *
 * <p>The class is annotated with `@ApplicationScoped` to indicate that it is a CDI bean
 * with application scope.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@ApplicationScoped
public class LLMRestClientFactory {

  /**
   * Produces an instance of `OllamaRestClient`.
   *
   * <p>This method retrieves the base URL for the client from the application's configuration.
   * If the configuration value is not set, it defaults to `http://localhost:11434`.
   *
   * @return an instance of `OllamaRestClient` configured with the base URL
   */
  @Produces
  @ApplicationScoped
  public OllamaRestClient produceClient() {
    // Retrieve the base URL from the configuration or use the default value
    String url =
        ConfigProvider.getConfig()
            .getOptionalValue("hyperapi.llm.ollama.base-url", String.class)
            .orElse("http://localhost:11434");

    // Build and return the REST client instance
    return QuarkusRestClientBuilder.newBuilder()
        .baseUri(URI.create(url))
        .build(OllamaRestClient.class);
  }
}