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
 * AbstractTypedEmitter is an abstract base class for emitting events of a specific type.
 *
 * <p>This class ensures that only entities of the specified type can be emitted.
 * It implements the `EntityEmitter` interface and provides a mechanism for type-safe event
 * emission.
 *
 * @param <T> the type of entity this emitter handles
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public abstract class AbstractTypedEmitter<T> implements EntityEmitter<T> {

  /**
   * The class type of the entity this emitter handles.
   */
  private final Class<T> entityType;

  /**
   * Constructs an `AbstractTypedEmitter` with the specified entity type.
   *
   * @param entityType the class type of the entity this emitter handles
   */
  protected AbstractTypedEmitter(Class<T> entityType) {
    this.entityType = entityType;
  }

  /**
   * Emits an event of the specified type for the given entity.
   *
   * <p>If the entity is an instance of the specified type or is null, the event
   * is passed to the `emitTyped` method for further processing.
   *
   * @param type   the type of the event
   * @param entity the entity associated with the event
   */
  @Override
  public void emit(EntityEvent.Type type, T entity) {
    if (entityType.isInstance(entity) || entity == null) {
      emitTyped(type, entity);
    }
  }

  /**
   * Emits a typed event for the given entity.
   *
   * <p>This method must be implemented by subclasses to define how the event
   * is processed.
   *
   * @param type   the type of the event
   * @param entity the entity associated with the event
   */
  protected abstract void emitTyped(EntityEvent.Type type, T entity);
}