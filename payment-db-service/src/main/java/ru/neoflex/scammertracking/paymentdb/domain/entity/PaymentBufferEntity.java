package ru.neoflex.scammertracking.paymentdb.domain.entity;

public class PaymentBufferEntity {

    public PaymentBufferEntity(Long id, long idPkPayment, String idCardNumber, int offsetPos) {
        this.id = id;
        this.idPkPayment = idPkPayment;
        this.idCardNumber = idCardNumber;
        this.offsetPos = offsetPos;
    }

    public PaymentBufferEntity() {
    }

    private Long id;
    private long idPkPayment;
    private String idCardNumber;
    private int offsetPos;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getIdPkPayment() {
        return idPkPayment;
    }

    public void setIdPkPayment(long idPkPayment) {
        this.idPkPayment = idPkPayment;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public int getOffsetPos() {
        return offsetPos;
    }

    public void setOffsetPos(int offsetPos) {
        this.offsetPos = offsetPos;
    }
}
