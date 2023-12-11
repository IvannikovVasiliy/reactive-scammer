package ru.neoflex.scammertracking.analyzer.domain.model;

public class Violation {

    public Violation(String fieldName, String message) {
        this.fieldName = fieldName;
        this.message = message;
    }

    public Violation() {
    }

    private String fieldName;
    private String message;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
