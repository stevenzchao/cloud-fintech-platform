package com.stevechao.tx_service.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<String> details = ex.getBindingResult().getAllErrors().stream()
        .map(error -> {
          if (error instanceof FieldError fieldError) {
            return fieldError.getField() + ": " + fieldError.getDefaultMessage();
          }
          return error.getDefaultMessage();
        })
        .toList();
    log.error("MethodArgumentNotValidException: ", ex);
    log.warn("Validation failed: {}", ex.getMessage());
    return response(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), details);
  }

  @ExceptionHandler(TransactionNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(TransactionNotFoundException ex, HttpServletRequest request) {
    log.error("TransactionNotFoundException: ", ex);
    log.warn("Error: {}", ex.getMessage());
    return response(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), List.of(ex.getMessage()));
  }

  @ExceptionHandler(IdempotencyConflictException.class)
  public ResponseEntity<ErrorResponse> handleIdempotencyConflict(IdempotencyConflictException ex, HttpServletRequest request) {
    log.error("IdempotencyConflictException: ", ex);
    log.warn("Error: {}", ex.getMessage());
    return response(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), List.of(ex.getMessage()));
  }

  @ExceptionHandler({MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
  public ResponseEntity<ErrorResponse> handleBadInput(Exception ex, HttpServletRequest request) {
    log.error("MethodArgumentTypeMismatchException: ", ex);
    log.warn("Invalid request parameter: {}", ex.getMessage());
    return response(HttpStatus.BAD_REQUEST, "Invalid request parameter", request.getRequestURI(), List.of(ex.getMessage()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
    log.error("DataIntegrityViolationException: ", ex);
    log.warn("Unique constraint violated: {}", ex.getMessage());
    return response(HttpStatus.CONFLICT, "Unique constraint violated", request.getRequestURI(), List.of(ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    log.error("Unhandled exception: ", ex);
    log.warn("Unexpected server error: {}", ex.getMessage());
    return response(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request.getRequestURI(), List.of(ex.getMessage()));
  }

  private ResponseEntity<ErrorResponse> response(HttpStatus status, String message, String path, List<String> details) {
    ErrorResponse error = new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path, details, MDC.get("requestId"));
    return ResponseEntity.status(status).body(error);
  }
}
