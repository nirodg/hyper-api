package dev.hyperapi.runtime.core.events;

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
 */
public interface EntityEmitter<E> {
    void emit(EntityEvent.Type type, E entity);
}
