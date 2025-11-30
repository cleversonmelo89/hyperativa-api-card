package com.hyperativa.cardapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CardRegisterRequest {
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^\\d+$", message = "Card number must contain only digits")
    private String cardNumber;
    
    private String batchNumber;
    private Integer sequenceNumber;
}


