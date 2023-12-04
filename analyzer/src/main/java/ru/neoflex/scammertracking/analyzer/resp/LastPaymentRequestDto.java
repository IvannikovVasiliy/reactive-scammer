package ru.neoflex.scammertracking.analyzer.resp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LastPaymentRequestDto {
    private long id;
    private String payerCardNumber;
    private String receiverCardNumber;
    private Coordinates coordinates;
    private LocalDateTime date;
}
