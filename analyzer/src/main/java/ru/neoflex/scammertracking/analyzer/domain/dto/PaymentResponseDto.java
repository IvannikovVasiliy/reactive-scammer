package ru.neoflex.scammertracking.analyzer.domain.dto;

import ru.neoflex.scammertracking.analyzer.domain.model.Coordinates;

import java.time.LocalDateTime;

public class PaymentResponseDto {

    public PaymentResponseDto(long id, String payerCardNumber, String receiverCardNumber,
                              Coordinates coordinates, LocalDateTime date, Boolean trusted) {
        this.id = id;
        this.payerCardNumber = payerCardNumber;
        this.receiverCardNumber = receiverCardNumber;
        this.coordinates = coordinates;
        this.date = date;
        this.trusted = trusted;
    }

    public PaymentResponseDto(PaymentResponseDto paymentResponseDto) {
        this(
                paymentResponseDto.getId(),
                paymentResponseDto.payerCardNumber,
                paymentResponseDto.receiverCardNumber,
                new Coordinates(paymentResponseDto.getCoordinates().getLatitude(), paymentResponseDto.getCoordinates().getLongitude()),
                paymentResponseDto.getDate(),
                paymentResponseDto.getTrusted()
        );
    }

    public PaymentResponseDto() {
    }

    private long id;
    private String payerCardNumber;
    private String receiverCardNumber;
    private Coordinates coordinates;
    private LocalDateTime date;
    private Boolean trusted;

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

    public Boolean getTrusted() {
        return trusted;
    }

    public void setTrusted(Boolean trusted) {
        this.trusted = trusted;
    }
}
