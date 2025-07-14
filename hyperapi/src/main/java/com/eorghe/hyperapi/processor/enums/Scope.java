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
package com.eorghe.hyperapi.processor.enums;

/**
 * Scope is an enumeration representing different CDI (Contexts and Dependency Injection) scopes.
 *
 * <p>This enum defines the standard scopes used in Jakarta EE applications, such as
 * ApplicationScoped, RequestScoped, SessionScoped, and DependentScoped.
 *
 * <p>Each scope is associated with its corresponding class name in the Jakarta EE framework.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public enum Scope {
  /**
   * Represents the ApplicationScoped context, which is active for the lifetime of the application.
   */
  APPLICATION("jakarta.enterprise.context.ApplicationScoped"),

  /**
   * Represents the RequestScoped context, which is active for the duration of a single HTTP
   * request.
   */
  REQUEST("jakarta.enterprise.context.RequestScoped"),

  /**
   * Represents the SessionScoped context, which is active for the duration of a user session.
   */
  SESSION("jakarta.enterprise.context.SessionScoped"),

  /**
   * Represents the DependentScoped context, which is active for the lifetime of the dependent
   * object.
   */
  DEPENDENT("jakarta.enterprise.context.DependentScoped");

  private final String scopeClass;

  /**
   * Constructs a Scope enum with the specified class name.
   *
   * @param scopeClass the fully qualified class name of the scope
   */
  Scope(String scopeClass) {
    this.scopeClass = scopeClass;
  }

  /**
   * Retrieves the fully qualified class name of the scope.
   *
   * @return the class name of the scope
   */
  public String getScopeClass() {
    return scopeClass;
  }
}