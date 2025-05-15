package com.rainy.homebudgettracker.auth;

import com.rainy.homebudgettracker.user.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
public class JwtAuthenticationFilterProd extends OncePerRequestFilter implements JwtAuthenticationFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        String sub;
        try {
            sub = jwtService.getClaim(token, "sub");
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (sub != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String premiumStatus;
            try {
                premiumStatus = jwtService.getClaim(token, "custom:access_level_");
            } catch (Exception e) {
                premiumStatus = "basic";
            }
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (premiumStatus.equals("premium")) {
                authorities.add(new SimpleGrantedAuthority(Role.PREMIUM_USER.name()));
            } else {
                authorities.add(new SimpleGrantedAuthority(Role.USER.name()));
            }
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    sub, token, authorities);
            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
