package vn.edu.fpt.fschool.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.access-token-expiration-ms:900000}")
  private long accessTokenExpirationMs;

  @Value("${jwt.refresh-token-expiration-ms:604800000}")
  private long refreshTokenExpirationMs;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(Long userId, String email, String role, Integer tokenVersion) {
    return Jwts.builder()
        .subject(email)
        .claim("userId", userId)
        .claim("role", role)
        .claim("tokenVersion", tokenVersion != null ? tokenVersion : 1)
        .claim("tokenType", "ACCESS")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
        .signWith(getSigningKey())
        .compact();
  }

  public String generateAccessToken(Long userId, String email, String role) {
    return generateAccessToken(userId, email, role, 1);
  }

  public String generateRefreshToken(Long userId, String email) {
    return Jwts.builder()
        .subject(email)
        .claim("userId", userId)
        .claim("tokenType", "REFRESH")
        .id(java.util.UUID.randomUUID().toString())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
        .signWith(getSigningKey())
        .compact();
  }

  public String generateToken(Long userId, String email, String role) {
    return generateAccessToken(userId, email, role);
  }

  public String getTokenType(String token) {
    try {
      return parseToken(token).get("tokenType", String.class);
    } catch (Exception e) {
      return null;
    }
  }

  public long getAccessTokenExpirationMs() {
    return accessTokenExpirationMs;
  }

  public long getRefreshTokenExpirationMs() {
    return refreshTokenExpirationMs;
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
    return parseToken(token).get("role", String.class);
  }

  public Long getUserIdFromToken(String token) {
    return parseToken(token).get("userId", Long.class);
  }

  public Integer getTokenVersionFromToken(String token) {
    try {
      Object version = parseToken(token).get("tokenVersion");
      if (version instanceof Number number) {
        return number.intValue();
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  public Date getExpirationFromToken(String token) {
    return parseToken(token).getExpiration();
  }

  public long getRemainingTtlMs(String token) {
    try {
      Date expiration = getExpirationFromToken(token);
      long diff = expiration.getTime() - System.currentTimeMillis();
      return Math.max(diff, 0L);
    } catch (Exception e) {
      return 0L;
    }
  }
}
