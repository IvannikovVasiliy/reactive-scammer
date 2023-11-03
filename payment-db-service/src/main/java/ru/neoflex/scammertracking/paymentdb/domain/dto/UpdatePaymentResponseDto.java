package ru.neoflex.scammertracking.paymentdb.domain.dto;

import ru.neoflex.scammertracking.paymentdb.domain.model.Coordinates;

import java.time.LocalDateTime;

public class UpdatePaymentResponseDto {

    public UpdatePaymentResponseDto(long id, String payerCardNumber, String receiverCardNumber, Coordinates coordinates, LocalDateTime date) {
        this.id = id;
        this.payerCardNumber = payerCardNumber;
        this.receiverCardNumber = receiverCardNumber;
        this.coordinates = coordinates;
        this.date = date;
    }

    public UpdatePaymentResponseDto() {
    }

    private long id;
    private String payerCardNumber;
    private String receiverCardNumber;
    private Coordinates coordinates;
    private LocalDateTime date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPayerCardNumber() {
        return payerCardNumber;
    }

    public void setPayerCardNumber(String payerCardNumber) {
        this.payerCardNumber = payerCardNumber;
    }

    public String getReceiverCardNumber() {
        return receiverCardNumber;
    }

    public void setReceiverCardNumber(String receiverCardNumber) {
        this.receiverCardNumber = receiverCardNumber;
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
