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

/**
 * Represents the details of a problem that occurred during API execution.
 *
 * <p>This class is used to encapsulate information about an error or issue
 * in a standardized format, including HTTP status code, title, detailed description, type URI, and
 * instance URI.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public class ProblemDetails {

  /**
   * A URI reference that identifies the problem type.
   */
  public String type;

  /**
   * A short, human-readable summary of the problem.
   */
  public String title;

  /**
   * The HTTP status code associated with the problem.
   */
  public int status;

  /**
   * A detailed explanation of the problem.
   */
  public String detail;

  /**
   * A URI reference that identifies the specific occurrence of the problem.
   */
  public String instance;

  /**
   * Constructs a new `ProblemDetails` object with the specified attributes.
   *
   * @param status   the HTTP status code
   * @param title    a short summary of the problem
   * @param detail   a detailed explanation of the problem
   * @param type     a URI reference identifying the problem type
   * @param instance a URI reference identifying the specific occurrence of the problem
   */
  public ProblemDetails(int status, String title, String detail, String type, String instance) {
    this.status = status;
    this.title = title;
    this.detail = detail;
    this.type = type;
    this.instance = instance;
  }
}