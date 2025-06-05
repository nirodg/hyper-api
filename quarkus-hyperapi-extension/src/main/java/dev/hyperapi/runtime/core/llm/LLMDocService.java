package dev.hyperapi.runtime.core.llm;

import io.smallrye.mutiny.Uni;

public interface LLMDocService {
  Uni<String> generateFor(Class<?> entityClass);

  Uni<String> generateOpenApiDoc(String prompt);
}
