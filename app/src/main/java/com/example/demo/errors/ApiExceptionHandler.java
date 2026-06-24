package com.example.demo.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setTitle("Resource not found");
    problem.setDetail(ex.getMessage());
    return problem;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Invalid request parameter");
    problem.setDetail(
        "Parameter '" + ex.getName() + "' has invalid value '" + ex.getValue() + "'.");
    return problem;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
    ex.printStackTrace();

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Malformed request");

    Throwable cause = ex.getCause();
    if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
      problem.setDetail(cause.getClass().getName() + ": " + cause.getMessage());
    } else {
      problem.setDetail(ex.getMessage());
    }

    return problem;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Bad request");
    problem.setDetail(ex.getMessage());
    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Validation failed");
    problem.setDetail("One or more request fields are invalid.");
    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception ex) {
    ex.printStackTrace();

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problem.setTitle("Internal server error");
    problem.setDetail("An unexpected error occurred.");
    return problem;
  }

  private String extractReadableMessage(HttpMessageNotReadableException ex) {
    Throwable cause = ex.getCause();
    if (cause == null) {
      return "Request body is invalid or malformed.";
    }

    String message = cause.getMessage();
    if (message == null || message.isBlank()) {
      return "Request body is invalid or malformed.";
    }

    if (message.contains("Unrecognized property")) {
      return "Request body contains an unknown field.";
    }

    return "Request body is invalid or malformed.";
  }
}
