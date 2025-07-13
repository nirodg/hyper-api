package dev.hyperapi.runtime.core.llm;

import dev.hyperapi.runtime.core.processor.annotations.Cache;
import dev.hyperapi.runtime.core.processor.annotations.Events;
import dev.hyperapi.runtime.core.processor.annotations.HyperResource;
import dev.hyperapi.runtime.core.processor.annotations.Pageable;
import dev.hyperapi.runtime.core.processor.annotations.Security;
import dev.hyperapi.runtime.core.registry.EntityRegistry;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class LLMDocsRoute {

    private static final Logger LOG = LoggerFactory.getLogger(LLMDocsRoute.class);

    @Inject
    LLMDocService docService;

    @Inject
    EntityRegistry registry;

    @Inject
    SmallRyeConfig config;

    public void register(io.vertx.ext.web.Router router) {
        if (!config.getOptionalValue("hyperapi.llm.docs.enabled", Boolean.class).orElse(false)) {
            LOG.info("LLM /docs/ai route is disabled via configuration.");
            return;
        }
        router.route("/docs/ai").handler(BodyHandler.create());
        router.get("/docs/ai").handler(this::generateOpenApiDocs);
        LOG.info("LLM /docs/ai route registered.");

    }

    private void generateOpenApiDocs(RoutingContext ctx) {

        Set<Class<?>> entities = registry.all();

        List<Uni<String>> tasks = entities.stream()
                .map(entity -> {

                    StringBuilder entitySpec = new StringBuilder("Entity: ")
                            .append(entity.getSimpleName()).append("\n")
                            .append("Base path: /api/").append(entity.getSimpleName().toLowerCase()).append("\n")
                            .append("CRUD Operations:\n");
                    HyperResource metadata = entity.getAnnotation(HyperResource.class);
                    String path = metadata != null && !metadata.path().isEmpty()
                            ? metadata.path()
                            : "/api/" + entity.getSimpleName().toLowerCase();


                    Pageable paging = metadata != null ? metadata.pageable() : null;
                    Security security = metadata != null ? metadata.security() : null;
                    Events events = metadata != null ? metadata.events() : null;
                    Cache cache = metadata != null ? metadata.cache() : null;

                    entitySpec.append("Entity: ").append(entity.getSimpleName()).append("\n")
                            .append("Base path: ").append(path).append("\n")
                            .append("CRUD Operations:\n");

                    // Paging
                    if (paging != null && paging.limit() > 0) {
                        entitySpec.append("- GET ").append(path)
                                .append(" → paginated (params: ?page, ?size; default size: ")
                                .append(paging.limit()).append(", max: ").append(paging.maxLimit()).append(")\n");
                    } else {
                        entitySpec.append("- GET ").append(path).append("\n");
                    }

                    // Standard CRUD
                    entitySpec.append("- GET ").append(path).append("/{id}\n")
                            .append("- POST ").append(path).append("\n")
                            .append("- PUT ").append(path).append("/{id}\n")
                            .append("- DELETE ").append(path).append("/{id}\n");

                    // Fields
                    entitySpec.append("Fields:\n");
                    for (Field field : entity.getDeclaredFields()) {
                        if (!field.getName().startsWith("$$")) {
                            entitySpec.append("  - ").append(field.getName())
                                    .append(" (type: ").append(field.getType().getSimpleName()).append(")\n");
                        }
                    }
                    // Security
                    if (security != null) {
                        entitySpec.append("Security:\n")
                                .append("  - Auth Required: ").append(security.requireAuth()).append("\n");
                    }
                    // Events
                    if (events != null) {
                        entitySpec.append("Event Broadcasting:\n")
                                .append("  - onCreate: ").append(events.onCreate()).append("\n")
                                .append("  - onUpdate: ").append(events.onUpdate()).append("\n")
                                .append("  - onDelete: ").append(events.onDelete()).append("\n");
                    }
                    // Cache
                    if (cache != null && cache.enabled()) {
                        entitySpec.append("Cache:\n")
                                .append("  - Enabled: true\n")
                                .append("  - TTL: ").append(cache.ttlSeconds()).append(" seconds\n");
                    }


                    return docService.generateOpenApiDoc(entitySpec.toString());
                })
                .toList();


        Uni.join()
                .all(tasks)
                .andCollectFailures()

                .onItem().transform(results ->
                        results.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining("\n\n---\n\n"))
                )
                .subscribe().with(
                        docs -> ctx.response()
                                .putHeader("Content-Type", "application/json")
                                .end(docs),
                        err -> {
                            LOG.error("Failed to generate LLM docs", err);
                            ctx.response().setStatusCode(500).end("[ERROR] Failed to generate documentation");
                        }
                );

    }


//    private void generateDocs(RoutingContext ctx) {
//        io.vertx.core.Vertx.currentContext().owner().executeBlocking(promise -> {
//            try {
//                Set<Class<?>> entities = registry.all();
//                String docs = entities.stream()
//                        .map(docService::generateFor)
//                        .reduce("", (a, b) -> a + "\n\n---\n\n" + b);
//                promise.complete(docs);
//            } catch (Exception e) {
//                promise.fail(e);
//            }
//        }, res -> {
//            if (res.succeeded()) {
//                ctx.response().putHeader("Content-Type", "text/plain").end((String) res.result());
//            } else {
//                LOG.error("Failed to generate LLM docs", res.cause());
//                ctx.response().setStatusCode(500).end("[ERROR] Failed to generate documentation");
//            }
//        });
//    }
}