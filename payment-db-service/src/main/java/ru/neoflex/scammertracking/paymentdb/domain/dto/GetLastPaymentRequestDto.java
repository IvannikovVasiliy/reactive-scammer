package ru.neoflex.scammertracking.paymentdb.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.websocket.OnMessage;

public class GetLastPaymentRequestDto {

    public GetLastPaymentRequestDto(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public GetLastPaymentRequestDto() {
    }

    @Size(min = 6, max = 60, message = "The length of cardNumber should be between 6 and 60")
    @NotNull
    private String cardNumber;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
