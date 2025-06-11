package dev.hyperapi.runtime.core.events;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class EntityEvent<BaseEntity> {

  public enum Type {
    CREATE,
    UPDATE,
    DELETE
  }

  private final Type type;
  private final BaseEntity entity;
  public EntityEvent(Type type, BaseEntity entity) {
    this.type = type;
    this.entity = entity;
  }
}
