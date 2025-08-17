package com.microbank.bankingservice.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private void attachCommon(ProblemDetail pd, HttpServletRequest request, Throwable ex) {
    pd.setProperty("timestamp", OffsetDateTime.now().toString());
    if (request != null) {
      pd.setInstance(URI.create(request.getRequestURI()));
      pd.setProperty("method", request.getMethod());
    }
    // Avoid leaking stack traces in response; rely on logs
    logAtLevel(pd.getStatus(), ex);
  }

  private void logAtLevel(Integer status, Throwable ex) {
    int s = status != null ? status : 500;
    if (s >= 500) {
      log.error("Unhandled exception", ex);
    } else if (s >= 400) {
      log.warn("Handled client error: {}", ex.getMessage());
      log.debug("Stacktrace for client error", ex);
    } else {
      log.info("Handled non-error exception: {}", ex.getMessage());
      log.debug("Stacktrace", ex);
    }
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Validation failed");
    pd.setDetail("One or more fields are invalid");

    Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            DefaultMessageSourceResolvable::getDefaultMessage,
            (a, b) -> a,
            LinkedHashMap::new));
    pd.setProperty("errors", errors);

    attachCommon(pd, request, ex);
    return pd;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Constraint violation");
    Map<String, String> errors = ex.getConstraintViolations().stream()
        .collect(Collectors.toMap(
            v -> pathOf(v),
            ConstraintViolation::getMessage,
            (a, b) -> a,
            LinkedHashMap::new));
    pd.setProperty("errors", errors);
    attachCommon(pd, request, ex);
    return pd;
  }

  private String pathOf(ConstraintViolation<?> v) {
    String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
    return path;
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ProblemDetail handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatus(ex.getStatusCode());
    pd.setTitle(ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString());
    pd.setDetail(ex.getMessage());
    attachCommon(pd, request, ex);
    return pd;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Malformed JSON request");
    pd.setDetail("Request body is missing or malformed");
    attachCommon(pd, request, ex);
    return pd;
  }

  @ExceptionHandler({AuthenticationException.class})
  public ProblemDetail handleAuth(AuthenticationException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    pd.setTitle("Unauthorized");
    pd.setDetail(ex.getMessage());
    attachCommon(pd, request, ex);
    return pd;
  }

  @ExceptionHandler({AccessDeniedException.class})
  public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    pd.setTitle("Forbidden");
    pd.setDetail(ex.getMessage());
    attachCommon(pd, request, ex);
    return pd;
  }

  @ExceptionHandler(JwtException.class)
  public ProblemDetail handleJwt(JwtException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    pd.setTitle("Invalid or missing token");
    pd.setDetail(ex.getMessage());
    attachCommon(pd, request, ex);
    return pd;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    pd.setTitle("Internal Server Error");
    pd.setDetail("An unexpected error occurred");
    attachCommon(pd, request, ex);
    return pd;
  }
}
