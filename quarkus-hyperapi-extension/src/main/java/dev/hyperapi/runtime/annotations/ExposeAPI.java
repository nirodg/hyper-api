package dev.hyperapi.runtime.annotations;

import jakarta.ws.rs.HttpMethod;
import java.lang.annotation.*;

/**
 * Marks a JPA entity to be exposed automatically by HyperAPI.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExposeAPI {

    /**
     * Base path override (defaults to entity simple name)
     */
    String path() default "";

    Class<?> dto() default Void.class; // 🆕 optional DTO override

    /**
     * Which HTTP methods will not be served (returning 404)
     */
    HttpMethodConfig disabledFor() default @HttpMethodConfig;

    /**
     * Which fields to ignore (for mapping)
     */
    Mapping mapping() default @Mapping;

    /**
     * Pagination settings
     */
    Pageable pageable() default @Pageable;

    /**
     * Fields to exclude from PATCH operations (compile-time validated against the entity fields)
     */
    Patchable patchable() default @Patchable;

     /*
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
      
    @interface HttpMethodConfig {
        /**
         * HTTP methods for which this API is disabled
         */
        jakarta.ws.rs.HttpMethod[] disabledFor() default {};
    }

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

    @interface Pageable {
        /**
         * Default page size
         */
        int limit() default 20;

        /**
         * Max page size allowed
         */
        int maxLimit() default 100;
    }

    @interface Patchable {
        /**
         * List of DTO attributes to exclude from PATCH
         */
        String[] exclude() default {};
    }

    @interface Security {
        /**
         * Roles allowed to call any endpoint
         */
        String[] rolesAllowed() default {};

        /**
         * If true ⇒ caller must at least be authenticated; if false ⇒ anonymous
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
