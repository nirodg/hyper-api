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
 * HttpMethod is an enumeration representing HTTP methods used in API requests.
 *
 * <p>This enum defines the standard HTTP methods that can be used to interact
 * with resources in a RESTful API.
 *
 * <ul>
 *   <li>GET: Used to retrieve data from a resource.</li>
 *   <li>POST: Used to create a new resource.</li>
 *   <li>PUT: Used to update an existing resource.</li>
 *   <li>PATCH: Used to partially update an existing resource.</li>
 *   <li>DELETE: Used to delete a resource.</li>
 * </ul>
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public enum HttpMethod {
  /**
   * Represents the HTTP GET method, used to retrieve data from a resource.
   */
  GET,

  /**
   * Represents the HTTP POST method, used to create a new resource.
   */
  POST,

  /**
   * Represents the HTTP PUT method, used to update an existing resource.
   */
  PUT,

  /**
   * Represents the HTTP PATCH method, used to partially update an existing resource.
   */
  PATCH,

  /**
   * Represents the HTTP DELETE method, used to delete a resource.
   */
  DELETE
}