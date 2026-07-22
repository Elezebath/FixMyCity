package lv.acnbootcamp.fixmycity.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Reads JWT access tokens from incoming requests and authenticates
 * users in Spring Security when the token is valid.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        log.debug("Processing request: {} {}", request.getMethod(), requestUri);
        log.debug("Authorization header present: {}", authorizationHeader != null);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            log.debug("No valid JWT token found in request");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());
        log.debug("JWT token extracted, length: {}", token.length());

        try {
            String email = jwtService.extractUsername(token);
            log.debug("Extracted email from token: {}", email);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetailsImpl userDetails =
                        (UserDetailsImpl) userDetailsService
                                .loadUserByUsername(email);

                if (jwtService.isTokenValid(token, userDetails)) {
                    log.debug("Token is valid for user: {}", email);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                    log.debug("User {} authenticated successfully with roles: {}", 
                            email, 
                            userDetails.getAuthorities());
                } else {
                    log.warn("Token validation failed for user: {}", email);
                }
            }
        } catch (JwtException | IllegalArgumentException exception) {
            // Invalid, expired, or malformed tokens must not authenticate the request.
            log.error("JWT authentication failed: {}", exception.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}