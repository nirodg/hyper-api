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
 * Cache is an annotation for enabling caching functionality in the application for a specific endpoint.
 *
 * <p>This annotation allows configuration of caching behavior, including enabling/disabling
 * caching and specifying the time-to-live (TTL) for cached items.
 *
 * <p>It is not implemented yet and serves as a placeholder for future functionality.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
// TODO NOT IMPLEMENTED YET
public @interface Cache {

  /**
   * Indicates whether caching is enabled.
   *
   * @return true if caching is enabled, false otherwise
   */
  boolean enabled() default false;

  /**
   * Specifies the time-to-live (TTL) for cached items in seconds.
   *
   * @return the TTL value in seconds, defaulting to 60 seconds
   */
  int ttlSeconds() default 60;
}