package com.hyperativa.cardapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EncryptionService Unit Tests")
class EncryptionServiceTest {

    private EncryptionService encryptionService;
    private static final String TEST_PASSWORD = "testPassword12345678901234567890";

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService(TEST_PASSWORD);
    }

    @Test
    @DisplayName("Deve criptografar e descriptografar com sucesso - Cenário Feliz")
    void testEncryptAndDecrypt_Success() {
        // Given
        String plainText = "4456897999999999";

        // When
        String encrypted = encryptionService.encrypt(plainText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("Deve gerar hash SHA-256 correto - Cenário Feliz")
    void testHashCardNumber_Success() {
        // Given
        String cardNumber = "4456897999999999";

        // When
        String hash1 = encryptionService.hashCardNumber(cardNumber);
        String hash2 = encryptionService.hashCardNumber(cardNumber);

        // Then
        assertNotNull(hash1);
        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Deve gerar hash diferente para números diferentes - Cenário Feliz")
    void testHashCardNumber_DifferentInputs() {
        // Given
        String cardNumber1 = "4456897999999999";
        String cardNumber2 = "4456897999999998";

        // When
        String hash1 = encryptionService.hashCardNumber(cardNumber1);
        String hash2 = encryptionService.hashCardNumber(cardNumber2);

        // Then
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Deve criptografar string vazia - Cenário Triste")
    void testEncrypt_EmptyString() {
        // Given
        String plainText = "";

        // When
        String encrypted = encryptionService.encrypt(plainText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertNotNull(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("Deve gerar hash para string vazia - Cenário Triste")
    void testHashCardNumber_EmptyString() {
        // Given
        String cardNumber = "";

        // When
        String hash = encryptionService.hashCardNumber(cardNumber);

        // Then
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("Deve criptografar números longos - Cenário Feliz")
    void testEncrypt_LongNumber() {
        // Given
        String plainText = "1234567890123456789012345678901234567890";

        // When
        String encrypted = encryptionService.encrypt(plainText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertNotNull(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("Deve gerar hash consistente para mesmo número - Cenário Feliz")
    void testHashCardNumber_Consistency() {
        // Given
        String cardNumber = "4456897999999999";

        // When
        String hash1 = encryptionService.hashCardNumber(cardNumber);
        String hash2 = encryptionService.hashCardNumber(cardNumber);
        String hash3 = encryptionService.hashCardNumber(cardNumber);

        // Then
        assertEquals(hash1, hash2);
        assertEquals(hash2, hash3);
    }
}

