package ru.neoflex.scammertracking.analyzer.domain.dto;

import lombok.Getter;
import lombok.Setter;
import ru.neoflex.scammertracking.analyzer.domain.model.Coordinates;

import java.util.Date;

@Getter
@Setter
public class SavePaymentRequestDto {

    public SavePaymentRequestDto(long id, String payerCardNumber, String receiverCardNumber, Coordinates coordinates, Date date) {
        this.id = id;
        this.payerCardNumber = payerCardNumber;
        this.receiverCardNumber = receiverCardNumber;
        this.coordinates = coordinates;
        this.date = date;
    }

    public SavePaymentRequestDto(SavePaymentRequestDto paymentRequestDto) {
        this(
                paymentRequestDto.getId(),
                paymentRequestDto.getPayerCardNumber(),
                paymentRequestDto.receiverCardNumber,
                new Coordinates(paymentRequestDto.getCoordinates().getLatitude(), paymentRequestDto.getCoordinates().getLongitude()),
                paymentRequestDto.getDate()
        );
    }

    public SavePaymentRequestDto() {
    }

    private long id;
    private String payerCardNumber;
    private String receiverCardNumber;
    private Coordinates coordinates;
    private Date date;
}