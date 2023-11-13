package ru.neoflex.scammertracking.analyzer.domain.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import ru.neoflex.scammertracking.analyzer.serdes.LocalDateTimeDeserializer;
import ru.neoflex.scammertracking.analyzer.serdes.LocalDateTimeSerializer;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
public class PaymentEntity implements Serializable {

    @Id
    private String payerCardNumber;
    private String receiverCardNumber;
    private Long idPayment;
    private double latitude;
    private double longitude;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime datePayment;

    public Long getIdPayment() {
        return idPayment;
    }

    public void setIdPayment(Long idPayment) {
        this.idPayment = idPayment;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getDatePayment() {
        return datePayment;
    }

    public void setDatePayment(LocalDateTime datePayment) {
        this.datePayment = datePayment;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.append("paymentEntity: {")
                .append("\"idPayment\": ").append(idPayment)
                .append("\"payerCardNumber\": ").append(payerCardNumber)
                .append("\"receiverCardNumber\": ").append(receiverCardNumber)
                .append("\"latitude\": ").append(latitude)
                .append("\"longitude\": ").append(longitude)
                .append("\"datePayment\": ").append(datePayment)
                .toString();
    }
}
