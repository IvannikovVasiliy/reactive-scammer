package ru.neoflex.scammertracking.paymentdb.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeletePaymentRequestDto {

    @NotNull
    private Long id;
}
