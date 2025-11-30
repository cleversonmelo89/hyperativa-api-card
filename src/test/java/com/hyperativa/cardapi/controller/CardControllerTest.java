package com.hyperativa.cardapi.controller;

import com.hyperativa.cardapi.dto.CardCheckRequest;
import com.hyperativa.cardapi.dto.CardCheckResponse;
import com.hyperativa.cardapi.dto.CardRegisterRequest;
import com.hyperativa.cardapi.dto.CardRegisterResponse;
import com.hyperativa.cardapi.service.CardService;
import com.hyperativa.cardapi.service.FileProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardController Unit Tests")
class CardControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private FileProcessingService fileProcessingService;

    @InjectMocks
    private CardController cardController;

    private CardRegisterRequest registerRequest;
    private CardCheckRequest checkRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new CardRegisterRequest();
        registerRequest.setCardNumber("4456897999999999");
        registerRequest.setBatchNumber("BATCH001");
        registerRequest.setSequenceNumber(1);

        checkRequest = new CardCheckRequest();
        checkRequest.setCardNumber("4456897999999999");
    }

    @Test
    @DisplayName("Deve registrar cartão com sucesso - Cenário Feliz")
    void testRegisterCard_Success() {
        // Given
        CardRegisterResponse response = CardRegisterResponse.builder()
                .id(1L)
                .message("Card registered successfully")
                .build();

        when(cardService.registerCard(anyString(), anyString(), any())).thenReturn(response);

        // When
        ResponseEntity<CardRegisterResponse> result = cardController.registerCard(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        assertEquals("Card registered successfully", result.getBody().getMessage());
        verify(cardService).registerCard(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Deve retornar erro quando serviço lança exceção - Cenário Triste")
    void testRegisterCard_ServiceException() {
        // Given
        when(cardService.registerCard(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            cardController.registerCard(registerRequest);
        });
        verify(cardService).registerCard(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Deve consultar cartão com sucesso - Cenário Feliz")
    void testCheckCard_Success() {
        // Given
        CardCheckResponse response = CardCheckResponse.builder()
                .exists(true)
                .cardId(1L)
                .message("Card found")
                .build();

        when(cardService.checkCard(anyString())).thenReturn(response);

        // When
        ResponseEntity<CardCheckResponse> result = cardController.checkCard(checkRequest);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isExists());
        assertEquals(1L, result.getBody().getCardId());
        assertEquals("Card found", result.getBody().getMessage());
        verify(cardService).checkCard(anyString());
    }

    @Test
    @DisplayName("Deve retornar não encontrado quando cartão não existe - Cenário Triste")
    void testCheckCard_NotFound() {
        // Given
        CardCheckResponse response = CardCheckResponse.builder()
                .exists(false)
                .cardId(null)
                .message("Card not found")
                .build();

        when(cardService.checkCard(anyString())).thenReturn(response);

        // When
        ResponseEntity<CardCheckResponse> result = cardController.checkCard(checkRequest);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isExists());
        assertNull(result.getBody().getCardId());
        assertEquals("Card not found", result.getBody().getMessage());
        verify(cardService).checkCard(anyString());
    }

    @Test
    @DisplayName("Deve retornar erro quando serviço lança exceção na consulta - Cenário Triste")
    void testCheckCard_ServiceException() {
        // Given
        when(cardService.checkCard(anyString()))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            cardController.checkCard(checkRequest);
        });
        verify(cardService).checkCard(anyString());
    }

    @Test
    @DisplayName("Deve processar arquivo com sucesso - Cenário Feliz")
    void testRegisterCardsFromFile_Success() {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        List<CardRegisterResponse> responses = List.of(
                CardRegisterResponse.builder()
                        .id(1L)
                        .message("Card registered successfully")
                        .build()
        );
        CompletableFuture<List<CardRegisterResponse>> future = CompletableFuture.completedFuture(responses);

        when(fileProcessingService.processFile(any())).thenReturn(future);

        // When
        ResponseEntity<List<CardRegisterResponse>> result = 
                cardController.registerCardsFromFile(file);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(1L, result.getBody().get(0).getId());
        verify(fileProcessingService).processFile(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando arquivo vazio - Cenário Triste")
    void testRegisterCardsFromFile_EmptyFile() {
        // Given
        MultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cardController.registerCardsFromFile(file);
        });
        verify(fileProcessingService, never()).processFile(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando arquivo não é TXT - Cenário Triste")
    void testRegisterCardsFromFile_InvalidFileType() {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cardController.registerCardsFromFile(file);
        });
        verify(fileProcessingService, never()).processFile(any());
    }

    @Test
    @DisplayName("Deve retornar erro quando serviço de arquivo lança exceção - Cenário Triste")
    void testRegisterCardsFromFile_ServiceException() {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        CompletableFuture<List<CardRegisterResponse>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Service error"));

        when(fileProcessingService.processFile(any())).thenReturn(future);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            cardController.registerCardsFromFile(file);
        });
        verify(fileProcessingService).processFile(any());
    }

    @Test
    @DisplayName("Deve retornar erro quando processamento é interrompido - Cenário Triste")
    void testRegisterCardsFromFile_Interrupted() {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        CompletableFuture<List<CardRegisterResponse>> future = new CompletableFuture<>();
        future.completeExceptionally(new InterruptedException("Processing interrupted"));

        when(fileProcessingService.processFile(any())).thenReturn(future);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            cardController.registerCardsFromFile(file);
        });
        verify(fileProcessingService).processFile(any());
    }
}
