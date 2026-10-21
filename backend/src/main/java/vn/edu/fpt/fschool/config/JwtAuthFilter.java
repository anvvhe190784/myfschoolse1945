package vn.edu.fpt.fschool.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtUtils jwtUtils;
  private final vn.edu.fpt.fschool.service.TokenBlacklistService tokenBlacklistService;
  private final vn.edu.fpt.fschool.service.TokenVersionService tokenVersionService;

  public JwtAuthFilter(
      JwtUtils jwtUtils,
      vn.edu.fpt.fschool.service.TokenBlacklistService tokenBlacklistService,
      vn.edu.fpt.fschool.service.TokenVersionService tokenVersionService) {
    this.jwtUtils = jwtUtils;
    this.tokenBlacklistService = tokenBlacklistService;
    this.tokenVersionService = tokenVersionService;
  }

  @Override
  @NullMarked
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String jwt = resolveBearerToken(request);
    if (jwt != null && !tokenBlacklistService.isBlacklisted(jwt)) {
      authenticateFromJwt(jwt, request);
    }

    filterChain.doFilter(request, response);
  }

  private String resolveBearerToken(HttpServletRequest request) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }

  private void authenticateFromJwt(String jwt, HttpServletRequest request) {
    var claimsOpt = jwtUtils.parseClaimsSafe(jwt);
    if (claimsOpt.isEmpty()) {
      return;
    }

    io.jsonwebtoken.Claims claims = claimsOpt.get();
    String tokenType = jwtUtils.getTokenType(claims);
    if ("REFRESH".equalsIgnoreCase(tokenType)) {
      return;
    }

    String email = claims.getSubject();
    Integer tokenVersion = jwtUtils.getTokenVersionFromClaims(claims);
    if (!tokenVersionService.isTokenVersionValid(email, tokenVersion)) {
      return;
    }

    setSecurityAuthentication(claims, email, request);
  }

  private void setSecurityAuthentication(
      io.jsonwebtoken.Claims claims, String email, HttpServletRequest request) {
    String role = jwtUtils.getRoleFromClaims(claims);
    if (role == null) {
      return;
    }

    // Spring Security role prefix convention: ROLE_<ROLE>
    String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityName);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            email, null, Collections.singletonList(authority));
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
