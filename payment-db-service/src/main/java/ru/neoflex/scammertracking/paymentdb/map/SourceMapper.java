package ru.neoflex.scammertracking.paymentdb.map;

import org.mapstruct.Mapper;
import ru.neoflex.scammertracking.paymentdb.domain.dto.SavePaymentResponseDto;
import ru.neoflex.scammertracking.paymentdb.domain.entity.PaymentEntity;

@Mapper
public interface SourceMapper {
    SavePaymentResponseDto sourceFromPaymentEntityToSavePaymentResponseDto(PaymentEntity paymentEntity);
}
