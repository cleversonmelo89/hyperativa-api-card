package com.hyperativa.cardapi.service;

import com.hyperativa.cardapi.dto.CardRegisterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileProcessingService Unit Tests")
class FileProcessingServiceTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private FileProcessingService fileProcessingService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(cardService);
    }

    @Test
    @DisplayName("Deve processar arquivo com sucesso - Cenário Feliz")
    void testProcessFile_Success() throws Exception {
        // Given - Linha precisa ter pelo menos 26 caracteres para ser processada
        // Formato: C[1-7] [8-26]número do cartão (posição fixa)
        String line1 = String.format("%-26s", "C2     4456897999999999"); // Garante 26 caracteres
        String line2 = String.format("%-26s", "C1     4456897922969999"); // Garante 26 caracteres
        String content = "DESAFIO-HYPERATIVA           20180524LOTE0001000010\n" +
                line1 + "\n" +
                line2 + "\n" +
                "LOTE0001000010";
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", content.getBytes());

        when(cardService.registerCard(anyString(), anyString(), any()))
                .thenReturn(CardRegisterResponse.builder().id(1L).message("Success").build())
                .thenReturn(CardRegisterResponse.builder().id(2L).message("Success").build());

        // When
        CompletableFuture<List<CardRegisterResponse>> future = fileProcessingService.processFile(file);
        List<CardRegisterResponse> result = future.get(5, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cardService, times(2)).registerCard(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Deve processar arquivo vazio - Cenário Triste")
    void testProcessFile_EmptyFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        // When
        CompletableFuture<List<CardRegisterResponse>> future = fileProcessingService.processFile(file);
        List<CardRegisterResponse> result = future.get(5, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(cardService, never()).registerCard(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Deve processar arquivo apenas com header - Cenário Triste")
    void testProcessFile_OnlyHeader() throws Exception {
        // Given
        String content = "DESAFIO-HYPERATIVA           20180524LOTE0001000010";
        MockMultipartFile file = new MockMultipartFile("file", "header.txt", "text/plain", content.getBytes());

        // When
        CompletableFuture<List<CardRegisterResponse>> future = fileProcessingService.processFile(file);
        List<CardRegisterResponse> result = future.get(5, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(cardService, never()).registerCard(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Deve ignorar linhas vazias - Cenário Feliz")
    void testProcessFile_WithEmptyLines() throws Exception {
        // Given - Linha precisa ter pelo menos 26 caracteres para ser processada
        String line1 = String.format("%-26s", "C2     4456897999999999");
        String line2 = String.format("%-26s", "C1     4456897922969999");
        String content = "DESAFIO-HYPERATIVA           20180524LOTE0001000010\n" +
                "\n" +
                line1 + "\n" +
                "\n" +
                line2;
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", content.getBytes());

        when(cardService.registerCard(anyString(), anyString(), any()))
                .thenReturn(CardRegisterResponse.builder().id(1L).build())
                .thenReturn(CardRegisterResponse.builder().id(2L).build());

        // When
        CompletableFuture<List<CardRegisterResponse>> future = fileProcessingService.processFile(file);
        List<CardRegisterResponse> result = future.get(5, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cardService, times(2)).registerCard(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Deve ignorar linhas com número de cartão inválido - Cenário Triste")
    void testProcessFile_InvalidCardNumber() throws Exception {
        // Given - Linha precisa ter pelo menos 26 caracteres para ser processada
        // A linha com ABC não será processada porque não passa no regex ^\d+$
        String line1 = String.format("%-26s", "C2     ABC1234567890123");
        String line2 = String.format("%-26s", "C1     4456897922969999");
        String content = "DESAFIO-HYPERATIVA           20180524LOTE0001000010\n" +
                line1 + "\n" +
                line2;
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", content.getBytes());

        when(cardService.registerCard(anyString(), anyString(), any()))
                .thenReturn(CardRegisterResponse.builder().id(1L).build());

        // When
        CompletableFuture<List<CardRegisterResponse>> future = fileProcessingService.processFile(file);
        List<CardRegisterResponse> result = future.get(5, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardService, times(1)).registerCard(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Deve processar arquivo com footer LOTE - Cenário Feliz")
    void testProcessFile_WithFooter() throws Exception {
        // Given - Linha precisa ter pelo menos 26 caracteres para ser processada
        String line1 = String.format("%-26s", "C2     4456897999999999");
        String line2 = String.format("%-26s", "C1     4456897922969999");
        String content = "DESAFIO-HYPERATIVA           20180524LOTE0001000010\n" +
                line1 + "\n" +
                "LOTE0001000010\n" +
                line2;
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", content.getBytes());

        when(cardService.registerCard(anyString(), anyString(), any()))
                .thenReturn(CardRegisterResponse.builder().id(1L).build())
                .thenReturn(CardRegisterResponse.builder().id(2L).build());

        // When
        CompletableFuture<List<CardRegisterResponse>> future = fileProcessingService.processFile(file);
        List<CardRegisterResponse> result = future.get(5, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cardService, times(2)).registerCard(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando erro ao ler arquivo - Cenário Triste")
    void testProcessFile_IOException() {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", new byte[0]) {
            @Override
            public java.io.InputStream getInputStream() throws java.io.IOException {
                throw new java.io.IOException("Error reading file");
            }
        };

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            CompletableFuture<List<CardRegisterResponse>> future = fileProcessingService.processFile(file);
            future.get(5, TimeUnit.SECONDS);
        });
    }
}

