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
package com.eorghe.hyperapi.processor.annotations;

/**
 * Security is an annotation used to configure security settings for API resources.
 *
 * <p>This annotation allows specifying roles that are permitted to access endpoints
 * and whether authentication is required for accessing the resource.
 *
 * <p>It is typically used to enforce security policies in HyperAPI applications.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
// TODO NOT YET IMPLEMENTED
public @interface Security {

  /**
   * Specifies the roles allowed to access any endpoint.
   *
   * <p>If this array is empty, no role-based restrictions are applied.
   *
   * @return an array of allowed roles
   */
  String[] rolesAllowed() default {};

  /**
   * Indicates whether authentication is required to access the resource.
   *
   * <p>If set to true, the caller must be authenticated. If set to false, anonymous
   * access is allowed unless `rolesAllowed` is non-empty.
   *
   * @return true if authentication is required, false otherwise
   */
  boolean requireAuth() default false;
}