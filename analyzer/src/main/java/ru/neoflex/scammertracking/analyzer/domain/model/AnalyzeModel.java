package ru.neoflex.scammertracking.analyzer.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnalyzeModel {
    private PaymentRequestDto paymentRequest;
    private LastPaymentResponseDto lastPayment;
}
