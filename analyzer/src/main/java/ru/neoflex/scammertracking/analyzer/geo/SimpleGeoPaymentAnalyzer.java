package ru.neoflex.scammertracking.analyzer.geo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.model.GeoPoint;

import java.time.LocalDateTime;

@Service
@Slf4j
public class SimpleGeoPaymentAnalyzer implements GeoAnalyzer {

    public boolean checkPayment(LastPaymentResponseDto lastPayment, PaymentRequestDto currentPayment) {
        log.info("Received for check payment. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }.\n currentPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date={} }",
                lastPayment.getId(), lastPayment.getPayerCardNumber(), lastPayment.getReceiverCardNumber(), lastPayment.getCoordinates().getLatitude(), lastPayment.getCoordinates().getLongitude(), lastPayment.getDate(), currentPayment.getId(), currentPayment.getPayerCardNumber(), currentPayment.getReceiverCardNumber(), currentPayment.getCoordinates().getLatitude(), currentPayment.getCoordinates().getLongitude(), currentPayment.getDate());

        LocalDateTime lastPaymentDate = lastPayment.getDate();
        LocalDateTime currentPaymentDate = currentPayment.getDate();
        GeoPoint lastGeoPoint = new GeoPoint(lastPayment.getCoordinates().getLatitude(), lastPayment.getCoordinates().getLongitude());
        GeoPoint currentGeoPoint = new GeoPoint(currentPayment.getCoordinates().getLatitude(), currentPayment.getCoordinates().getLongitude());

        double distance = GeoCoordinates.calculateDistance(lastGeoPoint, currentGeoPoint);

        if (lastPaymentDate.plusHours(1).compareTo(currentPaymentDate)>=0 && distance > 10000) {
            log.warn("The payment with id={} is suspicious", currentPayment.getId());
            return false;
        }
        if (lastPaymentDate.plusMinutes(1).compareTo(currentPaymentDate)>=0 && distance > 50) {
            log.warn("The payment with id={} is suspicious", currentPayment.getId());
            return false;
        }
        if (lastPaymentDate.plusSeconds(1).compareTo(currentPaymentDate)>=0 && distance > 1) {
            log.warn("The payment with id={} is suspicious", currentPayment.getId());
            return false;
        }

        log.info("The payment with id={} is trusted", currentPayment.getId());

        return true;
    }
}
