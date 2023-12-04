package ru.neoflex.scammertracking.paymentdb.map.impl;

import org.springframework.stereotype.Component;
import ru.neoflex.scammertracking.paymentdb.domain.dto.SavePaymentResponseDto;
import ru.neoflex.scammertracking.paymentdb.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.paymentdb.domain.model.Coordinates;
import ru.neoflex.scammertracking.paymentdb.map.SourceMapper;

import java.time.ZoneOffset;
import java.util.Date;

@Component
public class SourceMapperImplementation implements SourceMapper {

    public SavePaymentResponseDto sourceFromPaymentEntityToSavePaymentResponseDto(PaymentEntity paymentEntity) {
        if ( paymentEntity == null ) {
            return null;
        }

        SavePaymentResponseDto savePaymentResponseDto = new SavePaymentResponseDto();

        if ( paymentEntity.getId() != null ) {
            savePaymentResponseDto.setId( paymentEntity.getId() );
        }
        savePaymentResponseDto.setPayerCardNumber( paymentEntity.getPayerCardNumber() );
        savePaymentResponseDto.setReceiverCardNumber( paymentEntity.getReceiverCardNumber() );
        savePaymentResponseDto.setCoordinates(new Coordinates(paymentEntity.getLatitude(), paymentEntity.getLongitude()));
        if ( paymentEntity.getDate() != null ) {
            savePaymentResponseDto.setDate( Date.from( paymentEntity.getDate().toInstant( ZoneOffset.UTC ) ) );
        }

        return savePaymentResponseDto;
    }
}
