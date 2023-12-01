package ru.neoflex.scammertracking.paymentdb.domain.dto;

import lombok.Data;
import lombok.Getter;
import ru.neoflex.scammertracking.paymentdb.domain.model.Coordinates;

import java.time.LocalDateTime;

@Data
public class LastPaymentRequestDto {
    private long id;
    private String payerCardNumber;
    private String receiverCardNumber;
    private Coordinates coordinates;
    private LocalDateTime date;
}
