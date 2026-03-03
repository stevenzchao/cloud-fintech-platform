package com.stevechao.tx_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stevechao.tx_service.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public RestAuthEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
      throws IOException {

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    var body = new ErrorResponse(
        Instant.now(),
        401,
        "UNAUTHORIZED",
        "Missing/invalid token",
        request.getRequestURI(),
        List.of()
    );

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
