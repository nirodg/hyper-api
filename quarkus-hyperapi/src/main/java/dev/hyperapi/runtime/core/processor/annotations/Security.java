package dev.hyperapi.runtime.core.processor.annotations;

// TODO Not implemented yet
public @interface Security {
  /** Roles allowed to call any endpoint */
  String[] rolesAllowed() default {};

  /**
   * If true ⇒ caller must at least be authenticated; if false ⇒ anonymous allowed (unless
   * rolesAllowed is non-empty)
   */
  boolean requireAuth() default false;
}