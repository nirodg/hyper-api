package dev.hyperapi.runtime.core.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.hyperapi.runtime.annotations.RestService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.config.ConfigProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

@ApplicationScoped
public class OllamaLLMDocService implements LLMDocService {

    private static final Logger LOG = Logger.getLogger(OllamaLLMDocService.class);
    ObjectMapper mapper = new ObjectMapper();

    @Inject
    OllamaRestClient restClient;

    private final String model;

    public OllamaLLMDocService() {
        this.model = ConfigProvider.getConfig()
                .getOptionalValue("hyperapi.llm.ollama.model", String.class)
                .orElse("llama3");
    }

    @Override
    public Uni<String> generateOpenApiDoc(String prompt) {
        return Uni.createFrom().item(() -> {
                    ObjectNode payload = mapper.createObjectNode();
                    payload.put("model", model);
                    payload.put("prompt", prompt);
                    return payload;
                }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .map(payload -> {
                    try {
                        String response = restClient.sendPrompt(payload);
                        StringBuilder fullResponse = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new StringReader(response))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                JsonNode chunk = mapper.readTree(line);
                                fullResponse.append(chunk.path("response").asText());
                                if (chunk.path("done").asBoolean(false)) {
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            return "[ERROR: Failed to parse LLM response]";
                        }
                        return fullResponse.toString();
                    } catch (Exception e) {
                        LOG.error("Failed to call Ollama", e);
                        return "[ERROR] Could not generate documentation";
                    }
                });
    }

    @Override
    public Uni<String> generateFor(Class<?> entity) {
        String prompt = buildPrompt(entity);
        return generateOpenApiDoc(prompt);
    }

    private String buildPrompt(Class<?> entity) {


        Set<String> ignoredFields = Set.of();

        // Use DTO override if defined in @ExposeAPI
        RestService annotation = entity.getAnnotation(RestService.class);
        if (annotation != null && annotation.dto() != Void.class) {
            entity = annotation.dto();
        } else {
            assert annotation != null;
            ignoredFields = Set.of(annotation.mapping().ignore());
        }

        StringBuilder sb = new StringBuilder("""
                 Generate a complete OpenAPI3.0 schema given the provided information bellow. No additional text, just the schema.
                """);

        sb.append("\n\nClass: ").append(entity.getSimpleName()).append("\nFields:\n");

        for (Field field : entity.getDeclaredFields()) {
            // Skip internal, static, or synthetic fields
            if (field.getName().startsWith("$$")) continue;
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (field.isSynthetic()) continue;
            if (ignoredFields.contains(field.getName())) continue;

            sb.append("- ").append(field.getName())
                    .append(" (type: ").append(field.getType().getSimpleName()).append(")\n");
        }
        return sb.toString();
    }
}
