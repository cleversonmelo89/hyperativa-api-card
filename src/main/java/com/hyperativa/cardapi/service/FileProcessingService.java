package com.hyperativa.cardapi.service;

import com.hyperativa.cardapi.dto.CardRegisterResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessingService {

    private final CardService cardService;

    @Async
    public CompletableFuture<List<CardRegisterResponse>> processFile(MultipartFile file) {
        log.info("Starting file processing: {}", file.getOriginalFilename());
        List<CardRegisterResponse> responses = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            String batchNumber = null;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String originalLine = line;
                String trimmedLine = line.trim();
                
                if (trimmedLine.isEmpty()) {
                    continue;
                }
                
                if (originalLine.length() >= 45 &&
                    !trimmedLine.startsWith("C") && 
                    !trimmedLine.startsWith("LOTE")) {
                    batchNumber = originalLine.substring(37, 45).trim();
                    if (!batchNumber.isEmpty()) {
                        log.info("Processing batch: {}", batchNumber);
                    }
                    continue;
                }

                if (trimmedLine.startsWith("C")) {
                    try {
                        // [01-01]IDENTIFICADOR (C) [02-07]NUMERAÇÃO [08-26]NÚMERO DE CARTAO
                        // Tenta usar posições fixas primeiro, depois fallback para parsing flexível
                        String sequencePart = "";
                        String cardNumber = "";
                        
                        if (originalLine.length() >= 8) {
                            // [02-07] numeração (índices 1-6)
                            sequencePart = originalLine.substring(1, Math.min(7, originalLine.length())).trim();
                            
                            // [08-26] número do cartão (índices 7-25)
                            // Mas se a linha for maior, pega até o final (máximo 30 caracteres para evitar problemas)
                            int cardEndIndex = Math.min(26, originalLine.length());
                            // Se a linha tem mais de 26 caracteres, pode ser que o número do cartão seja maior
                            // Então pega até encontrar um espaço ou até 30 caracteres
                            if (originalLine.length() > 26) {
                                int spaceIndex = originalLine.indexOf(' ', 7);
                                if (spaceIndex > 0 && spaceIndex <= 30) {
                                    cardEndIndex = spaceIndex;
                                } else {
                                    cardEndIndex = Math.min(30, originalLine.length());
                                }
                            }
                            cardNumber = originalLine.substring(7, cardEndIndex).trim();
                        } else {
                            // Fallback: parsing flexível quando a linha não tem 26 caracteres
                            // Remove o "C" inicial
                            String withoutC = trimmedLine.substring(1).trim();
                            
                            // Procura por espaços para separar numeração do número do cartão
                            String[] parts = withoutC.split("\\s+");
                            if (parts.length >= 2) {
                                // Primeira parte é a numeração, resto é o número do cartão
                                sequencePart = parts[0];
                                cardNumber = withoutC.substring(parts[0].length()).trim();
                            } else if (parts.length == 1) {
                                // Só tem uma parte, assume que é o número do cartão
                                cardNumber = parts[0];
                            }
                        }

                        log.info("Line {}: sequencePart='{}', cardNumber='{}', originalLine.length={}, trimmedLine='{}'", 
                                lineNumber, sequencePart, cardNumber, originalLine.length(), trimmedLine);

                        if (!cardNumber.isEmpty() && cardNumber.matches("^\\d+$")) {
                            Integer sequenceNumber = parseSequenceNumber(sequencePart);
                            log.info("Processing card {}: number={}, batch={}, sequence={}", 
                                    lineNumber, cardNumber, batchNumber, sequenceNumber);
                            CardRegisterResponse response = cardService.registerCard(
                                    cardNumber, batchNumber, sequenceNumber);
                            // Adiciona informações adicionais da linha processada
                            response.setLineNumber(lineNumber);
                            response.setSequenceNumber(sequenceNumber);
                            responses.add(response);
                            log.info("Card {} registered successfully", cardNumber);
                        } else {
                            log.warn("Line {} skipped: cardNumber='{}' is empty or invalid (must be digits only)", 
                                    lineNumber, cardNumber);
                        }
                    } catch (Exception e) {
                        log.error("Error processing line {}: {}", lineNumber, e.getMessage(), e);
                    }
                }
                
                if (trimmedLine.startsWith("LOTE") && originalLine.length() >= 8) {
                    batchNumber = originalLine.substring(0, Math.min(8, originalLine.length())).trim();
                    if (!batchNumber.isEmpty()) {
                        log.info("Batch number from footer: {}", batchNumber);
                    }
                }
            }
            
            log.info("File processing completed. Processed {} cards", responses.size());
            return CompletableFuture.completedFuture(responses);
            
        } catch (Exception e) {
            log.error("Error processing file: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing file: " + e.getMessage(), e);
        }
    }

    private Integer parseSequenceNumber(String sequencePart) {
        try {
            String cleaned = sequencePart.replaceAll("[^0-9]", "");
            return cleaned.isEmpty() ? null : Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

