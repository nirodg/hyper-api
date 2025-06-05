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

    Patchable patchable() default @Patchable(exclude = {});

    Events events() default @Events(onCreate = false, onUpdate = false, onDelete = false);

    Cache cache() default @Cache(enabled = false, ttlSeconds = 60);

    Security security() default @Security(rolesAllowed = {}, requireAuth = false);

    /**
     * If you want to add MapStruct @Mapping annotations in the generated mapper,
     * list them here. Each entry refers to either the "toDto" or "toEntity" method,
     * and names the fields to ignore.
     */
    MapStructConfig[] mapstruct() default {};

    // New nested annotation:
    @interface MapStructConfig {
        /**
         * Which mapping method this applies to.
         * For now, we only support “TO_DTO” and “TO_ENTITY”.
         */
        Type type();

        /**
         * A list of DTO‐side or entity‐side properties to ignore.
         * If type=TO_DTO, each name here is a field on the target DTO;
         * if type=TO_ENTITY, each name is a field on the target Entity.
         */
        String[] ignoreNested() default {};
    }

    enum Type {
        /**
         * Generates @Mapping(target="X", ignore=true) inside toDto(...)
         */
        TO_DTO,
        /**
         * Generates @Mapping(target="Y", ignore=true) inside toEntity(...)
         */
        TO_ENTITY
    }

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
