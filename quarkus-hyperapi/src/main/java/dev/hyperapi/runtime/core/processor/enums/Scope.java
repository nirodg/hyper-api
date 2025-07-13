package dev.hyperapi.runtime.core.processor.enums;

public enum Scope {
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
