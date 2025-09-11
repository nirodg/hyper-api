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
package com.eorghe.hyperapi.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.Instant;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * HyperEntity is an abstract base class for JPA entities.
 *
 * <p>This class extends `PanacheEntity` and provides common fields and lifecycle methods
 * for tracking creation and update metadata.
 *
 * <p>It is annotated with `@MappedSuperclass` to indicate that its properties are inherited
 * by subclasses but it is not a standalone entity.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@Getter
@Setter
@MappedSuperclass
public abstract class HyperEntity extends PanacheEntity {

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
  private Instant createdOn;

  /**
   * The timestamp when the entity was last updated.
   */
  private Instant updatedOn;

  /**
   * Lifecycle callback method invoked before the entity is persisted.
   *
   * <p>This method sets the `createdOn` and `updatedOn` fields to the current date and time.
   */
  @PrePersist
  public void prePersist() {
      Instant now = Instant.now();
      createdOn = now;
      updatedOn = now;
  }

  /**
   * Lifecycle callback method invoked before the entity is updated.
   *
   * <p>This method updates the `updatedOn` field to the current date and time.
   */
  @PreUpdate
  public void preUpdate() {
    updatedOn = Instant.now();
  }

}