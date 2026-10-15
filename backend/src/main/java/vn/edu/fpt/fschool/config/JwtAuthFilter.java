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
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      String jwt = authHeader.substring(7);
      if (!tokenBlacklistService.isBlacklisted(jwt) && jwtUtils.validateToken(jwt)) {
        String tokenType = jwtUtils.getTokenType(jwt);
        if ("REFRESH".equalsIgnoreCase(tokenType)) {
          filterChain.doFilter(request, response);
          return;
        }
        String email = jwtUtils.getEmailFromToken(jwt);
        Integer tokenVersion = jwtUtils.getTokenVersionFromToken(jwt);

        // Chặn nhanh Access Token cũ nếu token_version không hợp lệ (đã đổi mật khẩu)
        if (!tokenVersionService.isTokenVersionValid(email, tokenVersion)) {
          filterChain.doFilter(request, response);
          return;
        }

        String role = jwtUtils.getRoleFromToken(jwt);

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

    filterChain.doFilter(request, response);
  }
}
