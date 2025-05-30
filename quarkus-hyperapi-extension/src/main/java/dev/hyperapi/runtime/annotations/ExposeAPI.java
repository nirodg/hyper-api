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
     * base path override (defaults to entity simple name)
     */
    String path() default "";

    Class<?> dto() default Void.class; // 🆕 optional DTO override

    /**
     * which fields to ignore
     */
    Mapping mapping() default @Mapping;

    /**
     * pagination settings
     */
    Pageable pageable() default @Pageable;

    /**
     * security roles
     */
    Security security() default @Security;

    /**
     * fire CDI events
     */
    Events events() default @Events;

    /**
     * caching configuration
     */
    Cache cache() default @Cache;

    @interface Mapping {

        String[] ignore() default {};
    }

    @interface Pageable {

        /**
         * default page size
         */
        int limit() default 20;

        /**
         * max page size allowed
         */
        int maxLimit() default 100;
    }

    @interface Security {

        /**
         * roles allowed to call any endpoint
         */
        String[] rolesAllowed() default {};

        
        /**
         * if true ⇒ caller must at least be authenticated; if false ⇒ anonymous
         * allowed (unless rolesAllowed is non-empty)
         */
        boolean requireAuth() default false;
    }

    @interface Events {

        boolean onCreate() default false;

        boolean onUpdate() default false;

        boolean onDelete() default false;
    }

    @interface Cache {

        boolean enabled() default false;

        /**
         * TTL in seconds
         */
        int ttlSeconds() default 60;
    }
}
