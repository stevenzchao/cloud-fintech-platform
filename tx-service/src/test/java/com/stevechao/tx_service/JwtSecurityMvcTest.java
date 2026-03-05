package com.stevechao.tx_service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stevechao.tx_service.error.ErrorResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest
@AutoConfigureMockMvc//??
public class JwtSecurityMvcTest {

  @Autowired
  MockMvc mockMvc;

  @Value("${security.jwt.secret}")
  String secret;

  @Value("${security.jwt.issuer}")
  String issuer;

  @Value("${security.jwt.audience}")
  String audience;

  private String jwt(Date exp,List<String> roles) {
    Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    return Jwts.builder()
        .setIssuer(issuer)
        .setAudience(audience)
        .setIssuedAt(Date.from(Instant.now()))
        .setExpiration(exp)
        .addClaims(Map.of("roles", roles))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }


  @Test
  void test_noToken_return401() throws Exception {
    UUID id = UUID.randomUUID();
    mockMvc.perform(get("/v1/transactions/{id}", id))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.message").value("Missing/invalid token"))
        .andExpect(jsonPath("$.path").value("/v1/transactions/" + id));
  }

  @Test
  void test_expiredToken_return401() throws Exception {
    UUID id = UUID.randomUUID();
    String expired = jwt(Date.from(Instant.now().minusSeconds(60)),new ArrayList<>());
    mockMvc.perform(get("/v1/transactions/{id}", id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + expired))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.message").value("Missing/invalid token"))
        .andExpect(jsonPath("$.path").value("/v1/transactions/" + id));
  }

  @Test
  void test_return404() throws Exception {
    UUID id = UUID.randomUUID();
    List<String> roles = new ArrayList<>();
    roles.add("ROLE_USER");
    String valid = jwt(Date.from(Instant.now().plusSeconds(3600)),roles);
    mockMvc.perform(get("/v1/transactions/{id}", id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + valid))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("transaction not found: " + id))
        .andExpect(jsonPath("$.path").value("/v1/transactions/" + id));
  }

  @Test
  void test_return403() throws Exception {
    UUID id = UUID.randomUUID();
    List<String> roles = new ArrayList<>();
    roles.add("ROLE_GUEST");
    String valid = jwt(Date.from(Instant.now().plusSeconds(3600)),roles);
    mockMvc.perform(get("/v1/transactions/{id}", id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + valid))
        .andExpect(status().isForbidden())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.error").value("FORBIDDEN"))
        .andExpect(jsonPath("$.message").value("Insufficient permissions"))
        .andExpect(jsonPath("$.path").value("/v1/transactions/" + id));
  }


}
