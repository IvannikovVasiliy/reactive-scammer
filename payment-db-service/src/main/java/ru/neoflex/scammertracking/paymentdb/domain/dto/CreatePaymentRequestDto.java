package ru.neoflex.scammertracking.paymentdb.domain.dto;

public class CreatePaymentRequestDto {

    public CreatePaymentRequestDto(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public CreatePaymentRequestDto() {
    }

    private String idCardNumber;

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }
}
