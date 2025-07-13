package dev.hyperapi.runtime.core.events;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EntityEvent<BaseEntity> {

  public enum Type {
    CREATE,
    UPDATE,
    DELETE,
    PATCH
  }

  private final Type type;
  private final BaseEntity entity;
  public EntityEvent(Type type, BaseEntity entity) {
    this.type = type;
    this.entity = entity;
  }
}
