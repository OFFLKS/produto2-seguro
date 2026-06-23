package com.locadora.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORREÇÃO: Configuração central de segurança.
 *
 * Vulnerabilidades corrigidas aqui:
 * 1. BCryptPasswordEncoder — senhas nunca armazenadas em texto plano.
 * 2. CORS restrito — origens explícitas em vez de "*".
 * 3. Controle de acesso por role — DELETE e PUT de jogos exigem ADMIN.
 * 4. Headers HTTP de segurança adicionados automaticamente pelo Spring Security:
 *    X-Content-Type-Options, X-Frame-Options, X-XSS-Protection.
 * 5. CSRF desabilitado apenas para API REST stateless (aceitável com tokens).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt com strength 12 — resistente a ataques de força bruta
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF desabilitado para REST stateless; em produção, usar JWT com CSRF token
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/jogos", "/api/jogos/**").permitAll()
                .requestMatchers("/", "/index.html", "/**.html", "/**.css", "/**.js").permitAll()
                // CORREÇÃO Controle de Acesso: DELETE e gerenciamento exigem ADMIN
                .requestMatchers(HttpMethod.DELETE, "/api/jogos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/jogos").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/jogos/**").hasRole("ADMIN")
                // Demais endpoints exigem autenticação
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {})
            // Segurança de headers HTTP
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(ct -> {})
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // CORREÇÃO CORS: origens explícitas — não usar "*" em produção
        config.setAllowedOrigins(List.of("http://localhost:8080", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
