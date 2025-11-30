package com.hyperativa.cardapi.security;

import com.hyperativa.cardapi.dto.AuthRequest;
import com.hyperativa.cardapi.dto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthController authController;

    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("admin123");
    }

    @Test
    @DisplayName("Deve fazer login com sucesso - Cenário Feliz")
    void testLogin_Success() {
        // Given
        UserDetails userDetails = User.builder()
                .username("admin")
                .password("password")
                .authorities(new SimpleGrantedAuthority("ROLE_CARD_REGISTER"))
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken(anyString(), any())).thenReturn("testToken");

        // When
        AuthResponse result = authController.login(authRequest);

        // Then
        assertNotNull(result);
        assertEquals("testToken", result.getToken());
        assertEquals("admin", result.getUsername());
        assertNotNull(result.getRoles());
        assertTrue(result.getRoles().contains("CARD_REGISTER"));
        verify(authenticationManager).authenticate(any());
        verify(tokenProvider).generateToken(anyString(), any());
    }

    @Test
    @DisplayName("Deve retornar erro com credenciais inválidas - Cenário Triste")
    void testLogin_InvalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authController.login(authRequest);
        });
        verify(authenticationManager).authenticate(any());
        verify(tokenProvider, never()).generateToken(anyString(), any());
    }

    @Test
    @DisplayName("Deve retornar token com múltiplas roles - Cenário Feliz")
    void testLogin_WithMultipleRoles() {
        // Given
        UserDetails userDetails = User.builder()
                .username("admin")
                .password("password")
                .authorities(
                        new SimpleGrantedAuthority("ROLE_CARD_REGISTER"),
                        new SimpleGrantedAuthority("ROLE_CARD_QUERY")
                )
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken(anyString(), any())).thenReturn("testToken");

        // When
        AuthResponse result = authController.login(authRequest);

        // Then
        assertNotNull(result);
        assertEquals("testToken", result.getToken());
        assertEquals("admin", result.getUsername());
        assertNotNull(result.getRoles());
        assertEquals(2, result.getRoles().size());
        assertTrue(result.getRoles().contains("CARD_REGISTER"));
        assertTrue(result.getRoles().contains("CARD_QUERY"));
        verify(authenticationManager).authenticate(any());
        verify(tokenProvider).generateToken(anyString(), any());
    }

    @Test
    @DisplayName("Deve retornar erro quando autenticação falha - Cenário Triste")
    void testLogin_AuthenticationFailure() {
        // Given
        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("Authentication failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authController.login(authRequest);
        });
        verify(authenticationManager).authenticate(any());
        verify(tokenProvider, never()).generateToken(anyString(), any());
    }

    @Test
    @DisplayName("Deve retornar erro quando geração de token falha - Cenário Triste")
    void testLogin_TokenGenerationFailure() {
        // Given
        UserDetails userDetails = User.builder()
                .username("admin")
                .password("password")
                .authorities(new SimpleGrantedAuthority("ROLE_CARD_REGISTER"))
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken(anyString(), any()))
                .thenThrow(new RuntimeException("Token generation failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authController.login(authRequest);
        });
        verify(authenticationManager).authenticate(any());
        verify(tokenProvider).generateToken(anyString(), any());
    }
}
