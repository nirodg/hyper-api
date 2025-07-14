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
 * Pageable is an annotation used to configure pagination settings for API resources.
 *
 * <p>This annotation allows specifying the default page size and the maximum page size
 * allowed for paginated API responses.
 *
 * <p>It is typically used in conjunction with other annotations to enable pagination
 * functionality in HyperAPI.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public @interface Pageable {

  /**
   * Specifies the default page size for paginated responses.
   *
   * @return the default page size, defaulting to 20
   */
  int limit() default 20;

  /**
   * Specifies the maximum page size allowed for paginated responses.
   *
   * @return the maximum page size, defaulting to 100
   */
  int maxLimit() default 100;
}