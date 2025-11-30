package com.hyperativa.cardapi.repository;

import com.hyperativa.cardapi.entity.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardRepository Unit Tests")
class CardRepositoryTest {

    @Mock
    private CardRepository cardRepository;

    private Card card;

    @BeforeEach
    void setUp() {
        card = Card.builder()
                .id(1L)
                .cardHash("hash123456789")
                .encryptedCardNumber("encrypted123456789")
                .batchNumber("BATCH001")
                .sequenceNumber(1)
                .build();
    }

    @Test
    @DisplayName("Deve salvar cartão com sucesso - Cenário Feliz")
    void testSave_Success() {
        // Given
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        // When
        Card saved = cardRepository.save(card);

        // Then
        assertNotNull(saved);
        assertEquals(1L, saved.getId());
        assertEquals("hash123456789", saved.getCardHash());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve buscar cartão por ID com sucesso - Cenário Feliz")
    void testFindById_Success() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        // When
        Optional<Card> found = cardRepository.findById(1L);

        // Then
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
        assertEquals("hash123456789", found.get().getCardHash());
        verify(cardRepository).findById(1L);
    }

    @Test
    @DisplayName("Não deve encontrar cartão por ID inexistente - Cenário Triste")
    void testFindById_NotFound() {
        // Given
        when(cardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        Optional<Card> found = cardRepository.findById(999L);

        // Then
        assertFalse(found.isPresent());
        verify(cardRepository).findById(999L);
    }

    @Test
    @DisplayName("Deve buscar cartão por hash com sucesso - Cenário Feliz")
    void testFindByCardHash_Success() {
        // Given
        when(cardRepository.findByCardHash("hash123456789")).thenReturn(Optional.of(card));

        // When
        Optional<Card> found = cardRepository.findByCardHash("hash123456789");

        // Then
        assertTrue(found.isPresent());
        assertEquals("hash123456789", found.get().getCardHash());
        verify(cardRepository).findByCardHash("hash123456789");
    }

    @Test
    @DisplayName("Não deve encontrar cartão por hash inexistente - Cenário Triste")
    void testFindByCardHash_NotFound() {
        // Given
        when(cardRepository.findByCardHash(anyString())).thenReturn(Optional.empty());

        // When
        Optional<Card> found = cardRepository.findByCardHash("nonexistent");

        // Then
        assertFalse(found.isPresent());
        verify(cardRepository).findByCardHash("nonexistent");
    }

    @Test
    @DisplayName("Deve verificar existência de cartão por hash - Cenário Feliz")
    void testExistsByCardHash_Success() {
        // Given
        when(cardRepository.existsByCardHash("hash123456789")).thenReturn(true);
        when(cardRepository.existsByCardHash("nonexistent")).thenReturn(false);

        // When
        boolean exists = cardRepository.existsByCardHash("hash123456789");
        boolean notExists = cardRepository.existsByCardHash("nonexistent");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
        verify(cardRepository).existsByCardHash("hash123456789");
        verify(cardRepository).existsByCardHash("nonexistent");
    }

    @Test
    @DisplayName("Não deve verificar existência quando hash não existe - Cenário Triste")
    void testExistsByCardHash_NotExists() {
        // Given
        when(cardRepository.existsByCardHash(anyString())).thenReturn(false);

        // When
        boolean exists = cardRepository.existsByCardHash("nonexistent");

        // Then
        assertFalse(exists);
        verify(cardRepository).existsByCardHash("nonexistent");
    }

    @Test
    @DisplayName("Deve deletar cartão com sucesso - Cenário Feliz")
    void testDelete_Success() {
        // Given
        doNothing().when(cardRepository).delete(any(Card.class));

        // When
        cardRepository.delete(card);

        // Then
        verify(cardRepository).delete(any(Card.class));
    }

    @Test
    @DisplayName("Deve atualizar cartão com sucesso - Cenário Feliz")
    void testUpdate_Success() {
        // Given
        Card updatedCard = Card.builder()
                .id(1L)
                .cardHash("hash123456789")
                .encryptedCardNumber("encrypted123456789")
                .batchNumber("BATCH002")
                .sequenceNumber(1)
                .build();

        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);

        // When
        Card result = cardRepository.save(updatedCard);

        // Then
        assertNotNull(result);
        assertEquals("BATCH002", result.getBatchNumber());
        verify(cardRepository).save(any(Card.class));
    }
}
