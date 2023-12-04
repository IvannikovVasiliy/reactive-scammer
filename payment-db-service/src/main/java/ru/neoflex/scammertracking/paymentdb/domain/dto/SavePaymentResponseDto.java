package ru.neoflex.scammertracking.paymentdb.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.neoflex.scammertracking.paymentdb.domain.model.Coordinates;

import java.util.Date;

public class SavePaymentResponseDto {

    public SavePaymentResponseDto(long id, String payerCardNumber, String receiverCardNumber, Coordinates coordinates, Date date) {
        this.id = id;
        this.payerCardNumber = payerCardNumber;
        this.receiverCardNumber = receiverCardNumber;
        this.coordinates = coordinates;
        this.date = date;
    }

    public SavePaymentResponseDto() {
    }

    @NotNull
    private long id;
    @Size(min = 6, max = 50, message = "The length of payerCardNumber should be between 6 and 50")
    @NotNull
    private String payerCardNumber;
    @Size(min = 6, max = 50, message = "The length of receiverCardNumber should be between 6 and 50")
    @NotNull
    private String receiverCardNumber;
    @NotNull
    private Coordinates coordinates;
    @NotNull
    private Date date;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}