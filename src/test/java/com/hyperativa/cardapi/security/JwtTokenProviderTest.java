package com.hyperativa.cardapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "mySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLong";
    private static final long EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", EXPIRATION);
    }

    @Test
    @DisplayName("Deve gerar token com sucesso - Cenário Feliz")
    void testGenerateToken_Success() {
        // Given
        String username = "admin";
        List<String> roles = List.of("CARD_REGISTER", "CARD_QUERY");

        // When
        String token = jwtTokenProvider.generateToken(username, roles);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Deve extrair username do token - Cenário Feliz")
    void testGetUsernameFromToken_Success() {
        // Given
        String username = "admin";
        List<String> roles = List.of("CARD_REGISTER");
        String token = jwtTokenProvider.generateToken(username, roles);

        // When
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertNotNull(extractedUsername);
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Deve extrair roles do token - Cenário Feliz")
    void testGetRolesFromToken_Success() {
        // Given
        String username = "admin";
        List<String> roles = List.of("CARD_REGISTER", "CARD_QUERY");
        String token = jwtTokenProvider.generateToken(username, roles);

        // When
        List<String> extractedRoles = jwtTokenProvider.getRolesFromToken(token);

        // Then
        assertNotNull(extractedRoles);
        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.contains("CARD_REGISTER"));
        assertTrue(extractedRoles.contains("CARD_QUERY"));
    }

    @Test
    @DisplayName("Deve validar token válido - Cenário Feliz")
    void testValidateToken_ValidToken() {
        // Given
        String username = "admin";
        List<String> roles = List.of("CARD_REGISTER");
        String token = jwtTokenProvider.generateToken(username, roles);

        // When
        Boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertNotNull(isValid);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Deve invalidar token inválido - Cenário Triste")
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        Boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertNotNull(isValid);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve invalidar token null - Cenário Triste")
    void testValidateToken_NullToken() {
        // Given
        String nullToken = null;

        // When
        Boolean isValid = jwtTokenProvider.validateToken(nullToken);

        // Then
        assertNotNull(isValid);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve extrair data de expiração do token - Cenário Feliz")
    void testGetExpirationDateFromToken_Success() {
        // Given
        String username = "admin";
        List<String> roles = List.of("CARD_REGISTER");
        String token = jwtTokenProvider.generateToken(username, roles);

        // When
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        // Then
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para usuários diferentes - Cenário Feliz")
    void testGenerateToken_DifferentUsers() {
        // Given
        String username1 = "admin";
        String username2 = "user";
        List<String> roles = List.of("CARD_REGISTER");

        // When
        String token1 = jwtTokenProvider.generateToken(username1, roles);
        String token2 = jwtTokenProvider.generateToken(username2, roles);

        // Then
        assertNotEquals(token1, token2);
        assertEquals(username1, jwtTokenProvider.getUsernameFromToken(token1));
        assertEquals(username2, jwtTokenProvider.getUsernameFromToken(token2));
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para roles diferentes - Cenário Feliz")
    void testGenerateToken_DifferentRoles() {
        // Given
        String username = "admin";
        List<String> roles1 = List.of("CARD_REGISTER");
        List<String> roles2 = List.of("CARD_QUERY");

        // When
        String token1 = jwtTokenProvider.generateToken(username, roles1);
        String token2 = jwtTokenProvider.generateToken(username, roles2);

        // Then
        assertNotEquals(token1, token2);
        List<String> extractedRoles1 = jwtTokenProvider.getRolesFromToken(token1);
        List<String> extractedRoles2 = jwtTokenProvider.getRolesFromToken(token2);
        assertNotEquals(extractedRoles1, extractedRoles2);
    }
}

