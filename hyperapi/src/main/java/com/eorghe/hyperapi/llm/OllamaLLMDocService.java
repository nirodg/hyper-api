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

import com.eorghe.hyperapi.processor.annotations.HyperResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

/**
 * OllamaLLMDocService is a service for generating OpenAPI documentation using the Ollama language model.
 *
 * <p>This class implements the `LLMDocService` interface and provides methods to generate
 * OpenAPI documentation for entities and prompts using a language model.
 *
 * <p>The class is annotated with `@ApplicationScoped` to indicate that it is a CDI bean
 * with application scope.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@ApplicationScoped
public class OllamaLLMDocService implements LLMDocService {

  /** Logger instance for logging messages related to the service. */
  private static final Logger LOG = Logger.getLogger(OllamaLLMDocService.class);

  /** ObjectMapper instance for JSON processing. */
  ObjectMapper mapper = new ObjectMapper();

  /** REST client for interacting with the Ollama language model. */
  @Inject
  OllamaRestClient restClient;

  /** The name of the language model to use, retrieved from the application's configuration. */
  private final String model;

  /**
   * Constructs an instance of `OllamaLLMDocService`.
   *
   * <p>Initializes the language model name from the application's configuration, defaulting to "llama3".
   */
  public OllamaLLMDocService() {
    this.model =
        ConfigProvider.getConfig()
            .getOptionalValue("hyperapi.llm.ollama.model", String.class)
            .orElse("llama3");
  }

  /**
   * Generates OpenAPI documentation based on the provided prompt.
   *
   * <p>This method sends the prompt to the Ollama language model and processes the response
   * to extract the generated documentation.
   *
   * @param prompt the input prompt describing the desired OpenAPI documentation
   * @return a `Uni` containing the generated OpenAPI documentation as a `String`
   */
  @Override
  public Uni<String> generateOpenApiDoc(String prompt) {
    return Uni.createFrom()
        .item(
            () -> {
              ObjectNode payload = mapper.createObjectNode();
              payload.put("model", model);
              payload.put("prompt", prompt);
              return payload;
            })
        .runSubscriptionOn(Infrastructure.getDefaultExecutor())
        .map(
            payload -> {
              try {
                String response = restClient.sendPrompt(payload);
                StringBuilder fullResponse = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new StringReader(response))) {
                  String line;
                  while ((line = reader.readLine()) != null) {
                    JsonNode chunk = mapper.readTree(line);
                    fullResponse.append(chunk.path("response").asText());
                    if (chunk.path("done").asBoolean(false)) {
                      break;
                    }
                  }
                } catch (IOException e) {
                  return "[ERROR: Failed to parse LLM response]";
                }
                return fullResponse.toString();
              } catch (Exception e) {
                LOG.error("Failed to call Ollama", e);
                return "[ERROR] Could not generate documentation";
              }
            });
  }

  /**
   * Generates documentation for the specified entity class.
   *
   * <p>This method builds a prompt based on the entity's structure and fields, then
   * uses the language model to generate the documentation.
   *
   * @param entity the class for which documentation is to be generated
   * @return a `Uni` containing the generated documentation as a `String`
   */
  @Override
  public Uni<String> generateFor(Class<?> entity) {
    String prompt = "";
    try {
      prompt = buildPrompt(entity);
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(OllamaLLMDocService.class.getName())
          .log(Level.SEVERE, null, ex);
    }
    return generateOpenApiDoc(prompt);
  }

  /**
   * Builds a prompt for generating OpenAPI documentation based on the entity's structure.
   *
   * <p>This method inspects the entity's fields and annotations to construct a detailed
   * prompt for the language model.
   *
   * @param entity the class to analyze
   * @return a `String` containing the generated prompt
   * @throws ClassNotFoundException if the DTO class specified in the `@HyperResource` annotation cannot be found
   */
  private String buildPrompt(Class<?> entity) throws ClassNotFoundException {

    Set<String> ignoredFields = Set.of();

    // Use DTO override if defined in @HyperResource
    HyperResource annotation = entity.getAnnotation(HyperResource.class);
    if (annotation != null && annotation.dto() != "") {
      entity = Class.forName(annotation.dto());
    } else {
      assert annotation != null;
      ignoredFields = Set.of(annotation.mapping().ignore());
    }

    StringBuilder sb =
        new StringBuilder(
            """
                 Generate a complete OpenAPI3.0 schema given the provided information bellow. No additional text, just the schema.
                """);

    sb.append("\n\nClass: ").append(entity.getSimpleName()).append("\nFields:\n");

    for (Field field : entity.getDeclaredFields()) {
      // Skip internal, static, or synthetic fields
      if (field.getName().startsWith("$$")) {
        continue;
      }
      if (Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      if (field.isSynthetic()) {
        continue;
      }
      if (ignoredFields.contains(field.getName())) {
        continue;
      }

      sb.append("- ")
          .append(field.getName())
          .append(" (type: ")
          .append(field.getType().getSimpleName())
          .append(")\n");
    }
    return sb.toString();
  }
}