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

import io.smallrye.mutiny.Uni;

/**
 * LLMDocService is an interface for generating documentation using a language model.
 *
 * <p>This service provides methods to generate documentation for specific entity classes
 * and OpenAPI specifications based on a given prompt.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public interface LLMDocService {

  /**
   * Generates documentation for the specified entity class.
   *
   * <p>This method uses a language model to create documentation for the given class.
   *
   * @param entityClass the class for which documentation is to be generated
   * @return a `Uni` containing the generated documentation as a `String`
   */
  Uni<String> generateFor(Class<?> entityClass);

  /**
   * Generates OpenAPI documentation based on the provided prompt.
   *
   * <p>This method uses a language model to create OpenAPI documentation tailored to the prompt.
   *
   * @param prompt the input prompt describing the desired OpenAPI documentation
   * @return a `Uni` containing the generated OpenAPI documentation as a `String`
   */
  Uni<String> generateOpenApiDoc(String prompt);
}