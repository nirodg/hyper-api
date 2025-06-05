package dev.hyperapi.runtime.core.llm;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.config.ConfigProvider;

import java.net.URI;

@ApplicationScoped
public class LLMRestClientFactory {

  @Produces
  @ApplicationScoped
  public OllamaRestClient produceClient() {
    String url =
        ConfigProvider.getConfig()
            .getOptionalValue("hyperapi.llm.ollama.base-url", String.class)
            .orElse("http://localhost:11434");

    return QuarkusRestClientBuilder.newBuilder()
        .baseUri(URI.create(url))
        .build(OllamaRestClient.class);
  }
}
