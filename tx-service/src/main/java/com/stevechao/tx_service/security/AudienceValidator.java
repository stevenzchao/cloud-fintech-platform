package com.stevechao.tx_service.security;

import java.util.List;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

  private final String requiredAudience;

  public AudienceValidator(String requiredAudience) {
    this.requiredAudience = requiredAudience;
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    List<String> aud = jwt.getAudience();
    if (aud != null && aud.contains(requiredAudience)) {
      return OAuth2TokenValidatorResult.success();
    }
    OAuth2Error err = new OAuth2Error("invalid_token", "Invalid audience", null);
    return OAuth2TokenValidatorResult.failure(err);
  }
}
