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
 * Mapping is an annotation used to configure field-level mapping for JPA entities.
 *
 * <p>This annotation allows specifying fields to ignore during mapping operations,
 * as well as nested fields to ignore.
 *
 * <p>It is typically used in conjunction with other annotations to customize
 * the behavior of entity-to-DTO mapping.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public @interface Mapping {

  /**
   * Specifies the names of fields to ignore during mapping.
   *
   * @return an array of field names to ignore
   */
  String[] ignore() default {};

  /**
   * Specifies the names of nested fields to ignore during mapping.
   *
   * @return an array of nested field names to ignore
   */
  String[] ignoreNested() default {};
}