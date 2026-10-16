package vn.edu.fpt.fschool.config;

import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
    try {
      return authConfig.getAuthenticationManager();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to get AuthenticationManager", e);
    }
  }

  @Bean
  @SuppressWarnings(
      "java:S4502") // Stateless REST API using Bearer JWT tokens in Authorization headers; CSRF
  // protection is not required
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    try {
      http.cors(Customizer.withDefaults())
          .csrf(AbstractHttpConfigurer::disable)
          .sessionManagement(
              session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .headers(
              headers ->
                  headers
                      .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                      .contentTypeOptions(Customizer.withDefaults()))
          .authorizeHttpRequests(
              auth ->
                  auth
                      .dispatcherTypeMatchers(
                          jakarta.servlet.DispatcherType.FORWARD,
                          jakarta.servlet.DispatcherType.ERROR)
                      .permitAll()
                      // Public endpoints and static web assets
                      .requestMatchers(
                          "/api/v1/auth/login",
                          "/api/v1/auth/refresh-token",
                          "/api/v1/auth/logout",
                          "/api/v1/auth/forgot-password",
                          "/api/v1/auth/verify-otp",
                          "/api/v1/auth/reset-password",
                          "/admin",
                          "/admin/",
                          "/admin/**",
                          "/",
                          "/index.html",
                          "/favicon.ico",
                          "/**/*.css",
                          "/**/*.js",
                          "/**/*.png",
                          "/**/*.svg",
                          "/**/*.ico")
                      .permitAll()
                      // Admin restricted endpoints
                      .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/news")
                      .hasRole("ADMIN")
                      .requestMatchers("/api/v1/admin/**")
                      .hasRole("ADMIN")
                      // Other requests require authentication
                      .anyRequest()
                      .authenticated())
          .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

      return http.build();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to configure SecurityFilterChain", e);
    }
  }

  @Bean
  @SuppressWarnings(
      "HttpUrlsUsage") // Localhost and Android emulator loopback origins in local development
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(
        List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://10.0.2.2:*",
            "https://*.fpt.edu.vn"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
