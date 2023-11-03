package ru.neoflex.scammertracking.paymentdb.domain.dto;

import ru.neoflex.scammertracking.paymentdb.domain.model.Coordinates;

import java.time.LocalDateTime;

public class UpdatePaymentRequestDto {

    public UpdatePaymentRequestDto(long id, String cardNumber, String receiverCardNumber, Coordinates coordinates, LocalDateTime date) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.coordinates = coordinates;
        this.date = date;
    }

    public UpdatePaymentRequestDto() {
    }

    private long id;
    private String cardNumber;
    private Coordinates coordinates;
    private LocalDateTime date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
