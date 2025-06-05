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

    Scope scope() default Scope.APPLICATION;

    HttpMethodConfig disabledFor() default @HttpMethodConfig(disabledFor = {});

    Mapping mapping() default @Mapping(ignore = {});

    Pageable pageable() default @Pageable(limit = 20, maxLimit = 100);

    Events events() default @Events(onCreate = false, onUpdate = false, onDelete = false);

    Cache cache() default @Cache(enabled = false, ttlSeconds = 60);

    Security security() default @Security(rolesAllowed = {}, requireAuth = false);

    // TODO add support for custom headers
    @interface HttpMethodConfig {
        /**
         * HTTP methods for which this API is disabled
         */
        jakarta.ws.rs.HttpMethod[] disabledFor() default {};
    }

    @interface Mapping {
        String[] ignore() default {};
        String[] ignoreNested() default {};
    }

    // TODO add support for custom query params
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

    // TODO add support for custom security annotations
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

    // TODO add support for custom events
    @interface Events {
        boolean onCreate() default false;

        boolean onUpdate() default false;

        boolean onDelete() default false;
    }

    // TODO add support for custom cache annotations
    @interface Cache {
        boolean enabled() default false;

        /**
         * TTL in seconds
         */
        int ttlSeconds() default 60;
    }

    enum Scope {
        APPLICATION("jakarta.enterprise.context.ApplicationScoped"),
        REQUEST("jakarta.enterprise.context.RequestScoped"),
        SESSION("jakarta.enterprise.context.SessionScoped"),
        DEPENDENT("jakarta.enterprise.context.DependentScoped");

        private final String scopeClass;

        Scope(String scopeClass) {
            this.scopeClass = scopeClass;
        }

        public String getScopeClass() {
            return scopeClass;
        }
    }

}
