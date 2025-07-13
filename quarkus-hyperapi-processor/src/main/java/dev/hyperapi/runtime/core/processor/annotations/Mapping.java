package dev.hyperapi.runtime.core.processor.annotations;

public @interface Mapping {
  String[] ignore() default {};
  String[] ignoreNested() default {};
}
