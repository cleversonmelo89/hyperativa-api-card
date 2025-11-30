package com.hyperativa.cardapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardRegisterResponse {
    private Long id;
    private String message;
    private Integer lineNumber; // Número da linha no arquivo processado
    private Integer sequenceNumber; // Número de sequência no lote
    private Boolean alreadyExists; // Indica se o cartão já existia no banco
}


