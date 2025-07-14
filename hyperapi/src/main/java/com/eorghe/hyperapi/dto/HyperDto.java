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
package com.eorghe.hyperapi.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * HyperDto is an abstract base class for Data Transfer Objects (DTOs) in the application.
 *
 * <p>It provides common fields for tracking entity metadata, such as creation and update
 * information, and implements Serializable for object serialization.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@Getter
@Setter
public abstract class HyperDto implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L; // Unique identifier for serialization.

  /**
   * The unique identifier of the entity.
   */
  private Long id;

  /**
   * The username or identifier of the user who created the entity.
   */
  private String createdBy;

  /**
   * The username or identifier of the user who last updated the entity.
   */
  private String updatedBy;

  /**
   * The timestamp when the entity was created.
   */
  private Date createdOn;

  /**
   * The timestamp when the entity was last updated.
   */
  private Date updatedOn;

}