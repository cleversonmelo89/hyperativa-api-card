package com.hyperativa.cardapi.controller;

import com.hyperativa.cardapi.dto.CardCheckRequest;
import com.hyperativa.cardapi.dto.CardCheckResponse;
import com.hyperativa.cardapi.dto.CardRegisterRequest;
import com.hyperativa.cardapi.dto.CardRegisterResponse;
import com.hyperativa.cardapi.service.CardService;
import com.hyperativa.cardapi.service.FileProcessingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Slf4j
public class CardController {

    private final CardService cardService;
    private final FileProcessingService fileProcessingService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('CARD_REGISTER')")
    public ResponseEntity<CardRegisterResponse> registerCard(@Valid @RequestBody CardRegisterRequest request) {
        log.info("Received card registration request");
        CardRegisterResponse response = cardService.registerCard(
                request.getCardNumber(),
                request.getBatchNumber(),
                request.getSequenceNumber()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/file")
    @PreAuthorize("hasRole('CARD_REGISTER')")
    public ResponseEntity<List<CardRegisterResponse>> registerCardsFromFile(
            @RequestParam("file") MultipartFile file) {
        log.info("Received file upload request: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".txt")) {
            throw new IllegalArgumentException("File must be a .txt file");
        }
        
        try {
            CompletableFuture<List<CardRegisterResponse>> future = fileProcessingService.processFile(file);
            List<CardRegisterResponse> responses = future.get(); // Aguarda a conclusão do processamento
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("File processing was interrupted: {}", e.getMessage(), e);
            throw new RuntimeException("File processing was interrupted", e);
        } catch (ExecutionException e) {
            log.error("Error processing file: {}", e.getMessage(), e);
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("Error processing file: " + e.getMessage(), cause);
        }
    }

    @PostMapping("/check")
    @PreAuthorize("hasRole('CARD_QUERY')")
    public ResponseEntity<CardCheckResponse> checkCard(@Valid @RequestBody CardCheckRequest request) {
        log.info("Received card check request");
        CardCheckResponse response = cardService.checkCard(request.getCardNumber());
        return ResponseEntity.ok(response);
    }
}


