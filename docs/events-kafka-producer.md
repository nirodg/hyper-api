# Kafka Event Emission in HyperAPI

This guide explains how HyperAPI supports **Kafka-based event emission** using CDI-compatible event emitters and annotation-driven configuration.

---

## ✨ Use Case Overview

You have an entity called `Order` and you want to automatically emit a Kafka message whenever an order is created. HyperAPI generates the necessary service logic and lets you plug in custom emitters.

---

## ⚖️ User-Defined Setup

### 1. Define Entity and Annotations

```java
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "orders")
@HyperResource(
    path = "/orders",
    mapping = @Mapping(ignore = {"internalId"}),
    events = @Events(onCreate = true, emitter = KafkaEventsForOrder.class)
)
public class Order extends BaseEntity {
    // fields...
}
```

### 2. Define Kafka Event Emitter

```java
@ApplicationScoped
public class KafkaEventsForOrder extends AbstractTypedEmitter<Order> {

    @Inject
    @Channel("order-create")
    Emitter<Double> orderEmitter;

    protected KafkaEventsForOrder() {
        super(Order.class);
    }

    @Override
    protected void emitTyped(EntityEvent.Type type, Order entity) {
        log.info("Emitting event: %s for entity: %s%n", type, entity);
        orderEmitter.send(entity);
    }
}
```

---

## 📆 Generated Code

### OrderService (Autogenerated)

```java
@ApplicationScoped
public class OrderService extends BaseEntityService<Order, OrderDTO, OrderMapper> {

    @Inject
    private KafkaEventsForOrder emitter;

    public OrderService() {
        super(Order.class, OrderDTO.class);
    }

    @Transactional
    public OrderDTO create(OrderDTO dto) {
        OrderDTO result = (OrderDTO) super.create(dto);
        this.emitter.emit(Type.CREATE, ((OrderMapper) this.mapper).toEntity(result));
        return result;
    }
}
```

---


## 🚀 How it Works

* The `@HyperResource(events = @Events(...))` annotation instructs the code generator to emit a typed event.
* The `KafkaEventsForOrder` class extends `AbstractTypedEmitter` and defines how to send messages to Kafka.
* The generated `OrderService` includes a call to `emitter.emit(...)` upon successful entity creation.

---


## 📈 Extending for Update/Delete Events


| Event   | Types          | Requires Config |
|---------|----------------|-----------------|
| CREATE  | 	post-persist	 | onCreate=true   |
| UPDATE	 | post-merge	    | onUpdate=true   |
| DELETE  | 	post-remove	  | onDelete=true   |

---


You can use your preferred Kafka client setup (e.g., MicroProfile Reactive Messaging, Spring Kafka, Quarkus Kafka, etc.).
