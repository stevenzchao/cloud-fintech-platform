package com.stevechao.tx_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stevechao.tx_service.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public RestAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
      throws IOException {

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    var body = new ErrorResponse(
        Instant.now(),
        403,
        "FORBIDDEN",
        "Insufficient permissions",
        request.getRequestURI(),
        List.of(),
        MDC.get("requestId")
    );

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}