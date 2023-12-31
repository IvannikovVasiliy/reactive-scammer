package ru.neoflex.scammertracking.paymentdb.domain.entity;

import java.time.LocalDateTime;

public class PaymentEntity {
    public PaymentEntity(long id, String payerCardNumber, String receiverCardNumber, float latitude, float longitude, LocalDateTime date) {
        this.id = id;
        this.payerCardNumber = payerCardNumber;
        this.receiverCardNumber = receiverCardNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    public PaymentEntity() {
    }

    private long id;
    private String payerCardNumber;
    private String receiverCardNumber;
    private float latitude;
    private float longitude;
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

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
