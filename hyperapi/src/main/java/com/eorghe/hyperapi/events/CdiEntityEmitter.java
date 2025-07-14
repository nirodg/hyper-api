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

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

/**
 * CdiEntityEmitter is a CDI-based implementation of the `EntityEmitter` interface.
 *
 * <p>This class is responsible for emitting entity-related events using the CDI event system.
 * It is annotated with `@Dependent` to indicate that its lifecycle is dependent on the lifecycle of
 * the bean that injects it.
 *
 * @param <T> the type of entity this emitter handles
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@Dependent
public class CdiEntityEmitter<T> implements EntityEmitter<T> {

  /**
   * The CDI event used to fire entity-related events.
   */
  @Inject
  Event<EntityEvent<?>> event;

  /**
   * Emits an entity-related event using the CDI event system.
   *
   * <p>This method creates a new `EntityEvent` object with the specified type and entity,
   * and fires it using the injected CDI event.
   *
   * @param type   the type of the event
   * @param entity the entity associated with the event
   */
  @Override
  public void emit(EntityEvent.Type type, T entity) {
    event.fire(new EntityEvent<>(type, entity));
  }
}