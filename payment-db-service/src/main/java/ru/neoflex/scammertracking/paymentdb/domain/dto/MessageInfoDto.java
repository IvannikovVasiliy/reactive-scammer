package ru.neoflex.scammertracking.paymentdb.domain.dto;

public class MessageInfoDto {

    public MessageInfoDto(Integer errorCode, Integer respCode, String message) {
        this.errorCode = errorCode;
        this.respCode = respCode;
        this.message = message;
    }

    public MessageInfoDto(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public MessageInfoDto() {
    }

    private Integer errorCode;
    private Integer respCode;
    private String message;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public Integer getRespCode() {
        return respCode;
    }

    public void setRespCode(Integer respCode) {
        this.respCode = respCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
