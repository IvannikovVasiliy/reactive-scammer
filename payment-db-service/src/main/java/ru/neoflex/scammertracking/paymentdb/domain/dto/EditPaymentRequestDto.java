package ru.neoflex.scammertracking.paymentdb.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.neoflex.scammertracking.paymentdb.domain.model.Coordinates;

import java.util.Date;

@Getter
@Setter
public class EditPaymentRequestDto {

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
}
