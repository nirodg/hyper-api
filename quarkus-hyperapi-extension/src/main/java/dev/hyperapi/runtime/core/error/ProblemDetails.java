package dev.hyperapi.runtime.core.error;

import jakarta.json.bind.annotation.JsonbProperty;

public class ProblemDetails {
  public String type;
  public String title;
  public int status;
  public String detail;
  public String instance;

  public ProblemDetails(int status, String title, String detail, String type, String instance) {
    this.status = status;
    this.title = title;
    this.detail = detail;
    this.type = type;
    this.instance = instance;
  }
}
