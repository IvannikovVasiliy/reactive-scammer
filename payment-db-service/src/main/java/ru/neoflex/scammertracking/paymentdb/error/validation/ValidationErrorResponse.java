package ru.neoflex.scammertracking.paymentdb.error.validation;

import java.util.List;

public class ValidationErrorResponse {

    public ValidationErrorResponse(List<Violation> violations) {
        this.violations = violations;
    }

    public ValidationErrorResponse() {
    }

    private List<Violation> violations;

    public List<Violation> getViolations() {
        return violations;
    }

    public void setViolations(List<Violation> violations) {
        this.violations = violations;
    }
}
