package dev.hyperapi.runtime.core.registry;

import dev.hyperapi.runtime.annotations.ExposeAPI;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Entity;
import jakarta.ws.rs.NotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

/**
 * Discovers all @ExposeAPI JPA entities at build/runtime.
 */
@ApplicationScoped
public class EntityRegistry {

    @ConfigProperty(name = "hyperapi.scan-packages", defaultValue = "")
    String configuredPackages;          // comma-separated list or blank = scan all

    private Set<Class<?>> exposed;

    @PostConstruct
    void init() {
        ConfigurationBuilder cfg = new ConfigurationBuilder().addScanners(Scanners.TypesAnnotated);

        if (!configuredPackages.isBlank()) {
            cfg.forPackages(Arrays.stream(configuredPackages.split(","))
                    .map(String::trim)
                    .toArray(String[]::new));
        } // else scan everything reachable on class-path

        Reflections reflections = new Reflections(cfg);

        exposed = reflections.getTypesAnnotatedWith(Entity.class).stream()
                .filter(c -> c.isAnnotationPresent(ExposeAPI.class))
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<Class<?>> all() {
        return exposed;
    }

    public Optional<Class<?>> bySimpleName(String simple) {
        return exposed.stream()
                .filter(c -> c.getSimpleName().equalsIgnoreCase(simple))
                .findFirst();
    }

    public Class<?> resolve(String simple) {
        return bySimpleName(simple)
                .orElseThrow(() -> new NotFoundException("Entity not found: " + simple));
    }

}
