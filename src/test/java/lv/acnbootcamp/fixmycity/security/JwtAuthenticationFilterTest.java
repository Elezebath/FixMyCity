package lv.acnbootcamp.fixmycity.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.entity.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterWhenAuthorizationHeaderIsMissing() throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);

        verifyNoInteractions(userDetailsService);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldContinueFilterWhenAuthorizationHeaderIsNotBearer() throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Basic abc123");

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);

        verifyNoInteractions(userDetailsService);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid() throws Exception {

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.CITIZEN);
        user.setEnabled(true);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer validToken");

        when(jwtService.extractUsername("validToken"))
                .thenReturn("test@example.com");

        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("validToken", userDetails))
                .thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(jwtService).extractUsername("validToken");
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(jwtService).isTokenValid("validToken", userDetails);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(
                userDetails,
                SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid() throws Exception {

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.CITIZEN);
        user.setEnabled(true);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer invalidToken");

        when(jwtService.extractUsername("invalidToken"))
                .thenReturn("test@example.com");

        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("invalidToken", userDetails))
                .thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(jwtService).extractUsername("invalidToken");
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(jwtService).isTokenValid("invalidToken", userDetails);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldClearSecurityContextWhenJwtExceptionOccurs() throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer invalidToken");

        when(jwtService.extractUsername("invalidToken"))
                .thenThrow(new JwtException("Invalid token") { });

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(jwtService).extractUsername("invalidToken");

        verifyNoInteractions(userDetailsService);

        verify(jwtService, never()).isTokenValid(any(), any());

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenAuthenticationAlreadyExists() throws Exception {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "existingUser",
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer validToken");

        when(jwtService.extractUsername("validToken"))
                .thenReturn("test@example.com");

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(jwtService).extractUsername("validToken");

        verifyNoInteractions(userDetailsService);

        verify(jwtService, never()).isTokenValid(any(), any());

        assertSame(
                authentication,
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenUsernameIsNull() throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer validToken");

        when(jwtService.extractUsername("validToken"))
                .thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(jwtService).extractUsername("validToken");

        verifyNoInteractions(userDetailsService);

        verify(jwtService, never()).isTokenValid(any(), any());

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }
}