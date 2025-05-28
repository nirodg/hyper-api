package dev.hyperapi.runtime.annotations;

import java.lang.annotation.*;

/**
 * Marks a JPA entity to be exposed automatically by HyperAPI.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExposeAPI {

    /**
     * Optional custom base-path override (defaults to entity simple name).
     */
    String path() default "";

    /**
     * Field-mapping options.
     */
    Mapping mapping() default @Mapping;

    @interface Mapping {

        /**
         * Properties to ignore for both serialization and deserialization.
         */
        String[] ignore() default {};
    }
}
