package com.rainy.homebudgettracker.auth;

import com.rainy.homebudgettracker.user.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Order(2)
@Slf4j
public class JwtAuthenticationFilterDev extends OncePerRequestFilter implements JwtAuthenticationFilter {
    @Value("${aws.auth.development.static-token.token:0}")
    private String jwtToken;
    @Value("${aws.auth.development.static-token.claims.sub:0}")
    private String sub;
    @Value("${aws.auth.development.static-token.claims.custom_access_level_:0}")
    private String status;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null &&
                sub != null &&
                status != null &&
                authHeader.startsWith("Bearer ") &&
                !jwtToken.isEmpty()) {

            final String tokenFromHeader = authHeader.substring(7);
            if (jwtToken.equals(tokenFromHeader)) {
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if ("premium".equalsIgnoreCase(status)) {
                        authorities.add(new SimpleGrantedAuthority(Role.PREMIUM_USER.name()));
                    } else {
                        authorities.add(new SimpleGrantedAuthority(Role.USER.name()));
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            sub,
                            tokenFromHeader,
                            authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

                log.info("Request: '{}'. Sub: {}. Status: {}. Authenticated using STATIC token.", request.getRequestURI(), sub, status);
                filterChain.doFilter(request, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
