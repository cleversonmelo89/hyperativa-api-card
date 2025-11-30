package com.hyperativa.cardapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardCheckResponse {
    private boolean exists;
    private Long cardId;
    private String message;
}


