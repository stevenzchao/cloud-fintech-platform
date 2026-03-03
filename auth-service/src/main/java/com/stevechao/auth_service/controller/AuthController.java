package com.stevechao.auth_service.controller;

import com.stevechao.auth_service.security.JWTService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JWTService jwtService;

  public AuthController(AuthenticationManager authenticationManager, JWTService jwtService) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }


  @PostMapping("/auth/login")
  public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(req.username(), req.password())
    );

    // For Day 4: roles from Spring authorities
    List<String> roles = auth.getAuthorities().stream()
        .map(a -> a.getAuthority())
        .toList();

    String token = jwtService.issueToken(auth.getName(), roles);
    return ResponseEntity.ok(new TokenResponse("Bearer", token));
  }

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of(
        "service", "auth-service",
        "status", "UP",
        "timestamp", Instant.now().toString()
    );
  }

  @GetMapping("/v1/auth/ping")
  public Map<String, Object> ping() {
    return Map.of(
        "message", "auth-service pong",
        "timestamp", Instant.now().toString()
    );
  }

  public record LoginRequest(String username, String password) {}
  public record TokenResponse(String tokenType, String accessToken) {}
}
