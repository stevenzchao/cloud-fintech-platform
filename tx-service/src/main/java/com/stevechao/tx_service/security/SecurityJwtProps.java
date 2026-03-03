package com.stevechao.tx_service.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record SecurityJwtProps(String issuer, String audience, String secret) {}
