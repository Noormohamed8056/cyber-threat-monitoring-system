package com.project.security;

import com.project.entity.User;
import com.project.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // ==========================================
    // FILTER EVERY INCOMING HTTP REQUEST
    // ==========================================
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Get Authorization header
        final String authHeader = request
                .getHeader("Authorization");

        String email = null;
        String token = null;

        // Step 2: Check if header exists and starts with "Bearer "
        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {

            // Step 3: Extract token (remove "Bearer " prefix)
            token = authHeader.substring(7);

            try {
                // Step 4: Extract email from token
                email = jwtUtil.extractEmail(token);
            } catch (Exception e) {
                // Invalid token — log and continue
                // Request will be rejected by Security Config
                logger.warn("JWT token extraction failed: "
                        + e.getMessage());
            }
        }

        // Step 5: Validate token and set authentication
        if (email != null
                && SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            // Step 6: Load user from database
            User user = userRepository
                    .findByEmailAndIsActiveTrue(email)
                    .orElse(null);

            if (user != null
                    && jwtUtil.validateToken(token, email)) {

                // Step 7: Build authority from user role
                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name());

                // Step 8: Create authentication token
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                Collections.singletonList(authority)
                        );

                // Step 9: Set request details
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                // Step 10: Set authentication in Security Context
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
        }

        // Step 11: Continue filter chain
        filterChain.doFilter(request, response);
    }

    // ==========================================
    // SKIP FILTER FOR PUBLIC ENDPOINTS
    // ==========================================
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        String path = request.getServletPath();

        // Skip JWT filter for these public paths
        return path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/register")
                || path.startsWith("/api/public/");
    }
}
