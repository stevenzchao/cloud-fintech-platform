package com.stevechao.auth_service.security;

import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {

  private final JwtProps props;
  private final SecretKey key;

  public JWTService(JwtProps props){
    this.props = props;
    this.key = Keys.hmacShaKeyFor(props.secret.getBytes(StandardCharsets.UTF_8));
  }

  public String issueToken(String subject, List<String> roles){
    Instant now = Instant.now();
    Instant exp = now.plus(props.accessTokenMinutes(), ChronoUnit.MINUTES);

    return Jwts.builder()
        .setIssuer(props.issuer())
        .setAudience(props.audience())
        .setSubject(subject)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(exp))
        .addClaims(Map.of("roles", roles))
        .signWith(key)
        .compact();
  }


  @ConfigurationProperties(prefix = "security.jwt")
  public record JwtProps(String issuer,
                         String audience,
                         String secret,
                         long accessTokenMinutes){}

}
