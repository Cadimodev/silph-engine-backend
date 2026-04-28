package com.silphengine.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        // Ensure SecurityContext is clean before each test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Clean up after tests to prevent state leakage
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenAuthHeaderIsMissing() throws ServletException, IOException {
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenAuthHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        // Given
        request.addHeader("Authorization", "InvalidTokenFormat 12345");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_shouldAuthenticateUser_whenTokenIsValid() throws ServletException, IOException {
        // Given
        String jwt = "valid.jwt.token";
        String username = "testUser";
        request.addHeader("Authorization", "Bearer " + jwt);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        when(jwtService.extractUsername(jwt)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(jwt, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUsername(jwt);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtService).isTokenValid(jwt, userDetails);
        verify(filterChain).doFilter(request, response);

        // Assert that authentication was set in the SecurityContext
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void doFilterInternal_shouldNotAuthenticateUser_whenTokenIsInvalid() throws ServletException, IOException {
        // Given
        String jwt = "invalid.jwt.token";
        String username = "testUser";
        request.addHeader("Authorization", "Bearer " + jwt);

        UserDetails userDetails = mock(UserDetails.class);

        when(jwtService.extractUsername(jwt)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(jwt, userDetails)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUsername(jwt);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtService).isTokenValid(jwt, userDetails);
        verify(filterChain).doFilter(request, response);

        // Assert that authentication was NOT set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldNotAuthenticateUser_whenUsernameExtractionFails() throws ServletException, IOException {
        // Given
        String jwt = "bad.jwt.token";
        request.addHeader("Authorization", "Bearer " + jwt);

        when(jwtService.extractUsername(jwt)).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUsername(jwt);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldSkipAuthentication_whenUserIsAlreadyAuthenticated() throws ServletException, IOException {
        // Given
        String jwt = "valid.jwt.token";
        String username = "testUser";
        request.addHeader("Authorization", "Bearer " + jwt);

        when(jwtService.extractUsername(jwt)).thenReturn(username);

        // Simulate an already authenticated user (e.g., from another filter or session)
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("existingUser", null)
        );

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUsername(jwt);
        // It shouldn't load user details or validate token if already authenticated
        verifyNoInteractions(userDetailsService);
        verify(jwtService, never()).isTokenValid(any(), any());
        verify(filterChain).doFilter(request, response);
    }
}
