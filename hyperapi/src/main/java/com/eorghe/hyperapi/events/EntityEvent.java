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
package com.eorghe.hyperapi.events;


/**
 * EntityEvent represents an event related to a specific entity in the system.
 *
 * <p>This record encapsulates the type of the event and the entity associated with it.
 * It is used to track and handle changes to entities, such as creation, updates, deletion, or
 * patching.
 *
 * @param <BaseEntity> the type of the entity associated with the event
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public record EntityEvent<BaseEntity>(com.eorghe.hyperapi.events.EntityEvent.Type type,
                                      BaseEntity entity) {

  /**
   * Type defines the possible types of entity-related events.
   *
   * <p>These types include:
   * <ul>
   *   <li>CREATE: Indicates that an entity has been created.</li>
   *   <li>UPDATE: Indicates that an entity has been updated.</li>
   *   <li>DELETE: Indicates that an entity has been deleted.</li>
   *   <li>PATCH: Indicates that an entity has been patched.</li>
   * </ul>
   */
  public enum Type {
    CREATE,
    UPDATE,
    DELETE,
    PATCH
  }

}
