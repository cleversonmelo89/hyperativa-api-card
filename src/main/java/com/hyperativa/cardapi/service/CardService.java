package com.hyperativa.cardapi.service;

import com.hyperativa.cardapi.dto.CardCheckResponse;
import com.hyperativa.cardapi.dto.CardRegisterResponse;
import com.hyperativa.cardapi.entity.Card;
import com.hyperativa.cardapi.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;

    @Transactional
    public CardRegisterResponse registerCard(String cardNumber, String batchNumber, Integer sequenceNumber) {
        log.info("Registering card - Batch: {}, Sequence: {}", batchNumber, sequenceNumber);
        
        String cardHash = encryptionService.hashCardNumber(cardNumber);
        
        if (cardRepository.existsByCardHash(cardHash)) {
            log.warn("Card already exists in database");
            Optional<Card> existingCard = cardRepository.findByCardHash(cardHash);
            return CardRegisterResponse.builder()
                    .id(existingCard.map(Card::getId).orElse(null))
                    .message("Card already registered")
                    .alreadyExists(true)
                    .build();
        }

        String encryptedCardNumber = encryptionService.encrypt(cardNumber);
        
        Card card = Card.builder()
                .cardHash(cardHash)
                .encryptedCardNumber(encryptedCardNumber)
                .batchNumber(batchNumber)
                .sequenceNumber(sequenceNumber)
                .build();

        Card savedCard = cardRepository.save(card);
        log.info("Card registered successfully with ID: {}", savedCard.getId());

        return CardRegisterResponse.builder()
                .id(savedCard.getId())
                .message("Card registered successfully")
                .alreadyExists(false)
                .build();
    }

    @Transactional(readOnly = true)
    public CardCheckResponse checkCard(String cardNumber) {
        log.info("Checking card existence");
        
        String cardHash = encryptionService.hashCardNumber(cardNumber);
        Optional<Card> card = cardRepository.findByCardHash(cardHash);

        if (card.isPresent()) {
            log.info("Card found with ID: {}", card.get().getId());
            return CardCheckResponse.builder()
                    .exists(true)
                    .cardId(card.get().getId())
                    .message("Card found")
                    .build();
        }

        log.info("Card not found");
        return CardCheckResponse.builder()
                .exists(false)
                .cardId(null)
                .message("Card not found")
                .build();
    }
}


