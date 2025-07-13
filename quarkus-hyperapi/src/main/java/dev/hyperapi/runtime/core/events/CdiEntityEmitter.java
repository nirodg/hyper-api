package dev.hyperapi.runtime.core.events;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@Dependent
public class CdiEntityEmitter<T> implements EntityEmitter<T> {

  @Inject Event<EntityEvent<?>> event;

  @Override
  public void emit(EntityEvent.Type type, T entity) {
    event.fire(new EntityEvent<>(type, entity));
  }
}
