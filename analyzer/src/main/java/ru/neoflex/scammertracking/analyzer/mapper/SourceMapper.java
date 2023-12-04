package ru.neoflex.scammertracking.analyzer.mapper;

import org.mapstruct.Mapper;
import ru.neoflex.scammertracking.analyzer.domain.dto.*;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;

@Mapper
public interface SourceMapper {
    PaymentResponseDto sourceFromPaymentRequestDtoToPaymentResponseDto(PaymentRequestDto paymentRequestDto);
    LastPaymentResponseDto sourceFromPaymentEntityToLastPaymentResponseDto(PaymentEntity paymentEntity);
    SavePaymentRequestDto sourceFromPaymentRequestDtoToSavePaymentRequestDto(PaymentRequestDto paymentRequestDto);
    PaymentEntity sourceFromSavePaymentRequestDtoToPaymentEntity(SavePaymentRequestDto savePaymentRequestDto);
    PaymentResponseDto sourceFromLastPaymentResponseDtoToPaymentResponseDto(LastPaymentResponseDto lastPaymentResponseDto);
    PaymentEntity sourceFromSavePaymentResponseDtoToPaymentEntity(SavePaymentResponseDto savePaymentResponseDto);
}
