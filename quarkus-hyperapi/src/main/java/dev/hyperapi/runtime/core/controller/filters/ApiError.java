package dev.hyperapi.runtime.core.controller.filters;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiError {

  private final String timestamp;
  private final int status;
  private final String error;
  private final String message;
  private final String path;

  public ApiError(String timestamp, int status, String error, String message, String path) {
    this.timestamp = timestamp;
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
  }
}
