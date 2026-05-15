package com.university.backend.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.backend.common.error.ApiException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_LOGIN_PATH = "/api/v1/auth/login";
    private static final String AUTH_REFRESH_PATH = "/api/v1/auth/refresh";
    private static final String AUTH_LOGOUT_PATH = "/api/v1/auth/logout";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return AUTH_LOGIN_PATH.equals(path) || AUTH_REFRESH_PATH.equals(path) || AUTH_LOGOUT_PATH.equals(path);
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                String token = authorization.substring(7);
                AuthenticatedAccount account = jwtService.parseAccessToken(token);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    account,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + account.role().name()))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ApiException exception) {
                SecurityContextHolder.clearContext();
                ProblemDetail detail = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
                detail.setTitle(exception.getStatus().getReasonPhrase());
                response.setStatus(exception.getStatus().value());
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                objectMapper.writeValue(response.getOutputStream(), detail);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
