package vn.edu.fpt.fschool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Type-safe configuration properties for JWT authentication. */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secret,
    Long expirationMs,
    Long accessTokenExpirationMs,
    Long refreshTokenExpirationMs) {}
