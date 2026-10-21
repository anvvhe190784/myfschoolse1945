package vn.edu.fpt.fschool.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings({
  "java:S2143",
  "squid:S2143"
}) // JJWT 0.12.x ClaimsMutator requires java.util.Date for issuedAt and expiration claims
public class JwtUtils {

  private static final String CLAIM_USER_ID = "userId";
  private static final String CLAIM_TOKEN_TYPE = "tokenType";
  private static final String CLAIM_TOKEN_VERSION = "tokenVersion";
  private static final String CLAIM_ROLE = "role";

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Getter
  @Value("${jwt.access-token-expiration-ms:900000}")
  private long accessTokenExpirationMs;

  @Getter
  @Value("${jwt.refresh-token-expiration-ms:604800000}")
  private long refreshTokenExpirationMs;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(Long userId, String email, String role, Integer tokenVersion) {
    java.time.Instant now = java.time.Instant.now();
    return Jwts.builder()
        .subject(email)
        .claim(CLAIM_USER_ID, userId)
        .claim(CLAIM_ROLE, role)
        .claim(CLAIM_TOKEN_VERSION, tokenVersion != null ? tokenVersion : 1)
        .claim(CLAIM_TOKEN_TYPE, "ACCESS")
        .issuedAt(java.util.Date.from(now))
        .expiration(java.util.Date.from(now.plusMillis(accessTokenExpirationMs)))
        .signWith(getSigningKey())
        .compact();
  }

  public String generateRefreshToken(Long userId, String email) {
    java.time.Instant now = java.time.Instant.now();
    return Jwts.builder()
        .subject(email)
        .claim(CLAIM_USER_ID, userId)
        .claim(CLAIM_TOKEN_TYPE, "REFRESH")
        .id(java.util.UUID.randomUUID().toString())
        .issuedAt(java.util.Date.from(now))
        .expiration(java.util.Date.from(now.plusMillis(refreshTokenExpirationMs)))
        .signWith(getSigningKey())
        .compact();
  }

  public java.util.Optional<Claims> parseClaimsSafe(String token) {
    try {
      return java.util.Optional.of(parseToken(token));
    } catch (Exception e) {
      return java.util.Optional.empty();
    }
  }

  public String getTokenType(Claims claims) {
    return claims != null ? claims.get(CLAIM_TOKEN_TYPE, String.class) : null;
  }

  public String getRoleFromClaims(Claims claims) {
    return claims != null ? claims.get(CLAIM_ROLE, String.class) : null;
  }

  public Integer getTokenVersionFromClaims(Claims claims) {
    if (claims == null) {
      return null;
    }
    Object version = claims.get(CLAIM_TOKEN_VERSION);
    if (version instanceof Number number) {
      return number.intValue();
    }
    return null;
  }

  public String getTokenType(String token) {
    try {
      return parseToken(token).get(CLAIM_TOKEN_TYPE, String.class);
    } catch (Exception e) {
      return null;
    }
  }

  public Claims parseToken(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  public boolean validateToken(String token) {
    try {
      parseToken(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String getEmailFromToken(String token) {
    return parseToken(token).getSubject();
  }

  public String getRoleFromToken(String token) {
    return parseToken(token).get(CLAIM_ROLE, String.class);
  }

  public Integer getTokenVersionFromToken(String token) {
    try {
      Object version = parseToken(token).get(CLAIM_TOKEN_VERSION);
      if (version instanceof Number number) {
        return number.intValue();
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  public java.time.Instant getExpirationFromToken(String token) {
    java.util.Date expiration = parseToken(token).getExpiration();
    return expiration != null ? expiration.toInstant() : null;
  }

  public long getRemainingTtlMs(String token) {
    try {
      java.time.Instant expiration = getExpirationFromToken(token);
      if (expiration == null) {
        return 0L;
      }
      long diff = java.time.Duration.between(java.time.Instant.now(), expiration).toMillis();
      return Math.max(diff, 0L);
    } catch (Exception e) {
      return 0L;
    }
  }
}
