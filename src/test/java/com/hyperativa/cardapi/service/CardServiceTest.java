package com.hyperativa.cardapi.service;

import com.hyperativa.cardapi.dto.CardCheckResponse;
import com.hyperativa.cardapi.dto.CardRegisterResponse;
import com.hyperativa.cardapi.entity.Card;
import com.hyperativa.cardapi.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardService Unit Tests")
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CardService cardService;

    private String cardNumber;
    private String cardHash;
    private String encryptedCardNumber;

    @BeforeEach
    void setUp() {
        cardNumber = "4456897999999999";
        cardHash = "hash123456789";
        encryptedCardNumber = "encrypted123456789";
    }

    @Test
    @DisplayName("Deve registrar cartão com sucesso - Cenário Feliz")
    void testRegisterCard_Success() {
        // Given
        String batchNumber = "BATCH001";
        Integer sequenceNumber = 1;
        Card savedCard = Card.builder()
                .id(1L)
                .cardHash(cardHash)
                .encryptedCardNumber(encryptedCardNumber)
                .batchNumber(batchNumber)
                .sequenceNumber(sequenceNumber)
                .build();

        when(encryptionService.hashCardNumber(cardNumber)).thenReturn(cardHash);
        when(cardRepository.existsByCardHash(cardHash)).thenReturn(false);
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        // When
        CardRegisterResponse response = cardService.registerCard(cardNumber, batchNumber, sequenceNumber);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Card registered successfully", response.getMessage());
        verify(encryptionService).hashCardNumber(cardNumber);
        verify(cardRepository).existsByCardHash(cardHash);
        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve retornar erro quando cartão já existe - Cenário Triste")
    void testRegisterCard_AlreadyExists() {
        // Given
        String batchNumber = "BATCH001";
        Integer sequenceNumber = 1;
        Card existingCard = Card.builder()
                .id(1L)
                .cardHash(cardHash)
                .encryptedCardNumber(encryptedCardNumber)
                .build();

        when(encryptionService.hashCardNumber(cardNumber)).thenReturn(cardHash);
        when(cardRepository.existsByCardHash(cardHash)).thenReturn(true);
        when(cardRepository.findByCardHash(cardHash)).thenReturn(Optional.of(existingCard));

        // When
        CardRegisterResponse response = cardService.registerCard(cardNumber, batchNumber, sequenceNumber);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Card already registered", response.getMessage());
        verify(encryptionService).hashCardNumber(cardNumber);
        verify(cardRepository).existsByCardHash(cardHash);
        verify(cardRepository).findByCardHash(cardHash);
        verify(encryptionService, never()).encrypt(anyString());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve registrar cartão com batchNumber null - Cenário Feliz")
    void testRegisterCard_WithNullBatchNumber() {
        // Given
        Card savedCard = Card.builder()
                .id(1L)
                .cardHash(cardHash)
                .encryptedCardNumber(encryptedCardNumber)
                .build();

        when(encryptionService.hashCardNumber(cardNumber)).thenReturn(cardHash);
        when(cardRepository.existsByCardHash(cardHash)).thenReturn(false);
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        // When
        CardRegisterResponse response = cardService.registerCard(cardNumber, null, null);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Card registered successfully", response.getMessage());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve encontrar cartão na consulta - Cenário Feliz")
    void testCheckCard_Exists() {
        // Given
        Card card = Card.builder()
                .id(1L)
                .cardHash(cardHash)
                .encryptedCardNumber(encryptedCardNumber)
                .build();

        when(encryptionService.hashCardNumber(cardNumber)).thenReturn(cardHash);
        when(cardRepository.findByCardHash(cardHash)).thenReturn(Optional.of(card));

        // When
        CardCheckResponse response = cardService.checkCard(cardNumber);

        // Then
        assertNotNull(response);
        assertTrue(response.isExists());
        assertEquals(1L, response.getCardId());
        assertEquals("Card found", response.getMessage());
        verify(encryptionService).hashCardNumber(cardNumber);
        verify(cardRepository).findByCardHash(cardHash);
    }

    @Test
    @DisplayName("Não deve encontrar cartão na consulta - Cenário Triste")
    void testCheckCard_NotExists() {
        // Given
        when(encryptionService.hashCardNumber(cardNumber)).thenReturn(cardHash);
        when(cardRepository.findByCardHash(cardHash)).thenReturn(Optional.empty());

        // When
        CardCheckResponse response = cardService.checkCard(cardNumber);

        // Then
        assertNotNull(response);
        assertFalse(response.isExists());
        assertNull(response.getCardId());
        assertEquals("Card not found", response.getMessage());
        verify(encryptionService).hashCardNumber(cardNumber);
        verify(cardRepository).findByCardHash(cardHash);
    }

    @Test
    @DisplayName("Deve retornar null quando cartão existe mas não tem ID - Cenário Triste")
    void testRegisterCard_ExistsButNoId() {
        // Given
        when(encryptionService.hashCardNumber(cardNumber)).thenReturn(cardHash);
        when(cardRepository.existsByCardHash(cardHash)).thenReturn(true);
        when(cardRepository.findByCardHash(cardHash)).thenReturn(Optional.empty());

        // When
        CardRegisterResponse response = cardService.registerCard(cardNumber, "BATCH001", 1);

        // Then
        assertNotNull(response);
        assertNull(response.getId());
        assertEquals("Card already registered", response.getMessage());
        verify(cardRepository).findByCardHash(cardHash);
    }
}

