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
 * User-defined emitter: Kafka, WS, etc.
 * <p>Example usage:</p>
 * <pre>{@code
 * @Inject
 * @Channel("customer-events")
 * EntityEmitter<Customer> customerEmitter;
 *
 * public void updateCustomer(Customer customer) {
 *     customerEmitter.emit(EntityEvent.Type.UPDATE, customer);
 * }
 * }</pre>
 *
 * <p>The emitter creates messages with the following structure:</p>
 * <pre>{@code
 * {
 *   "type": "UPDATE",
 *   "entity": {
 *     // serialized entity fields
 *   }
 * }
 * }</pre>
 *
 * @param <E> the type of entity this emitter handles
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public interface EntityEmitter<E> {

  /**
   * Emits an event for the specified entity.
   *
   * <p>This method is responsible for creating and sending an event of the given type
   * for the provided entity. The implementation determines how the event is emitted.
   *
   * @param type the type of the event (e.g., CREATE, UPDATE, DELETE)
   * @param entity the entity associated with the event
   */
  void emit(EntityEvent.Type type, E entity);
}
