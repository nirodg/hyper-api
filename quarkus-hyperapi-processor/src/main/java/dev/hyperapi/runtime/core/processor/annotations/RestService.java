package dev.hyperapi.runtime.core.processor.annotations;


import java.lang.annotation.*;

/**
 * Marks a JPA entity to be exposed automatically by HyperAPI.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestService {

    String path() default "";
    String dto() default "";

    HttpMethodConfig disabledFor() default @HttpMethodConfig(disabledFor = {});

    Mapping mapping() default @Mapping(ignore = {});

    Pageable pageable() default @Pageable(limit = 20, maxLimit = 100);

    Patchable patchable() default @Patchable(exclude = {});

    Events events() default @Events(onCreate = false, onUpdate = false, onDelete = false);

    Cache cache() default @Cache(enabled = false, ttlSeconds = 60);

    Security security() default @Security(rolesAllowed = {}, requireAuth = false);

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
