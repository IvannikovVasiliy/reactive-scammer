package ru.neoflex.scammertracking.analyzer.mapper;

import org.springframework.stereotype.Component;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.analyzer.domain.model.Coordinates;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class SourceMapperImplementation {

    public PaymentResponseDto sourceFromPaymentRequestDtoToPaymentResponseDto(PaymentRequestDto paymentRequestDto) {
        if ( paymentRequestDto == null ) {
            return null;
        }

        PaymentResponseDto paymentResponseDto = new PaymentResponseDto();

        paymentResponseDto.setId( paymentRequestDto.getId() );
        paymentResponseDto.setPayerCardNumber( paymentRequestDto.getPayerCardNumber() );
        paymentResponseDto.setReceiverCardNumber( paymentRequestDto.getReceiverCardNumber() );
        paymentResponseDto.setCoordinates( paymentRequestDto.getCoordinates() );
        paymentResponseDto.setDate( paymentRequestDto.getDate() );

        return paymentResponseDto;
    }

    public LastPaymentResponseDto sourceFromPaymentEntityToLastPaymentResponseDto(PaymentEntity paymentEntity) {
        if ( paymentEntity == null ) {
            return null;
        }

        Coordinates coordinates = new Coordinates(paymentEntity.getLatitude(), paymentEntity.getLongitude());

        LastPaymentResponseDto lastPaymentResponseDto = new LastPaymentResponseDto();
        lastPaymentResponseDto.setId(paymentEntity.getIdPayment());
        lastPaymentResponseDto.setPayerCardNumber( paymentEntity.getPayerCardNumber() );
        lastPaymentResponseDto.setReceiverCardNumber( paymentEntity.getReceiverCardNumber() );
        lastPaymentResponseDto.setCoordinates(coordinates);
        lastPaymentResponseDto.setDate(paymentEntity.getDatePayment());

        return lastPaymentResponseDto;
    }

    public SavePaymentRequestDto sourceFromPaymentRequestDtoToSavePaymentRequestDto(PaymentRequestDto paymentRequestDto) {
        if ( paymentRequestDto == null ) {
            return null;
        }

        SavePaymentRequestDto savePaymentRequestDto = new SavePaymentRequestDto();
        savePaymentRequestDto.setId(paymentRequestDto.getId());
        savePaymentRequestDto.setPayerCardNumber(paymentRequestDto.getPayerCardNumber());
        savePaymentRequestDto.setReceiverCardNumber(paymentRequestDto.getReceiverCardNumber());
        savePaymentRequestDto.setCoordinates(paymentRequestDto.getCoordinates());
        savePaymentRequestDto.setDate(Date.from(paymentRequestDto.getDate().atZone(ZoneId.systemDefault()).toInstant()));

        return savePaymentRequestDto;
    }

    public PaymentEntity sourceFromSavePaymentRequestDtoToPaymentEntity(SavePaymentRequestDto savePaymentRequestDto) {
        if ( savePaymentRequestDto == null ) {
            return null;
        }

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setPayerCardNumber(savePaymentRequestDto.getPayerCardNumber());
        paymentEntity.setReceiverCardNumber(savePaymentRequestDto.getReceiverCardNumber());
        paymentEntity.setIdPayment(savePaymentRequestDto.getId());
        paymentEntity.setLatitude(savePaymentRequestDto.getCoordinates().getLatitude());
        paymentEntity.setLongitude(savePaymentRequestDto.getCoordinates().getLongitude());

        LocalDateTime datePayment = savePaymentRequestDto
                .getDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        paymentEntity.setDatePayment(datePayment);

        return paymentEntity;
    }
}
