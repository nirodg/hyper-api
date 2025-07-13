package dev.hyperapi.runtime.core.processor.annotations;

import dev.hyperapi.runtime.core.events.CdiEntityEmitter;
import dev.hyperapi.runtime.core.events.EntityEmitter;

public @interface Events {
  boolean onCreate() default false;

  boolean onUpdate() default false;

  boolean onDelete() default false;

  boolean onPatch() default false;

  Class<? extends EntityEmitter> emitter() default CdiEntityEmitter.class;
}
