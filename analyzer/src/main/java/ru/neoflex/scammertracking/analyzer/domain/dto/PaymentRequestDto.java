package ru.neoflex.scammertracking.analyzer.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.neoflex.scammertracking.analyzer.domain.model.Coordinates;
import ru.neoflex.scammertracking.analyzer.serdes.LocalDateTimeDeserializer;
import ru.neoflex.scammertracking.analyzer.serdes.LocalDateTimeSerializer;

import java.time.LocalDateTime;

public class PaymentRequestDto {

    public PaymentRequestDto(long id, String payerCardNumber, String receiverCardNumber, Coordinates coordinates, LocalDateTime date) {
        this.id = id;
        this.payerCardNumber = payerCardNumber;
        this.receiverCardNumber = receiverCardNumber;
        this.coordinates = coordinates;
        this.date = date;
    }

    public PaymentRequestDto(PaymentRequestDto paymentRequestDto) {
        this(
                paymentRequestDto.getId(),
                paymentRequestDto.getPayerCardNumber(),
                paymentRequestDto.receiverCardNumber,
                new Coordinates(paymentRequestDto.getCoordinates().getLatitude(), paymentRequestDto.getCoordinates().getLongitude()),
                paymentRequestDto.getDate()
        );
    }

    public PaymentRequestDto() {
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

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getDate() {
        return date;
    }

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
