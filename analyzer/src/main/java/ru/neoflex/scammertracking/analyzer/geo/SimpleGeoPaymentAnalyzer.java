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
        LocalDateTime lastPaymentDate = lastPayment.getDate();
        LocalDateTime currentPaymentDate = currentPayment.getDate();
        GeoPoint lastGeoPoint = new GeoPoint(lastPayment.getCoordinates().getLatitude(), lastPayment.getCoordinates().getLongitude());
        GeoPoint currentGeoPoint = new GeoPoint(currentPayment.getCoordinates().getLatitude(), currentPayment.getCoordinates().getLongitude());

        double distance = GeoCoordinates.calculateDistance(lastGeoPoint, currentGeoPoint);

        if (!lastPaymentDate.plusHours(1).isBefore(currentPaymentDate) && distance > 10000) {
            log.warn("The payment with id={} is suspicious", currentPayment.getId());
            return false;
        }
        if (!lastPaymentDate.plusMinutes(1).isBefore(currentPaymentDate) && distance > 50) {
            log.warn("The payment with id={} is suspicious", currentPayment.getId());
            return false;
        }
        if (!lastPaymentDate.plusSeconds(1).isBefore(currentPaymentDate) && distance > 1) {
            log.warn("The payment with id={} is suspicious", currentPayment.getId());
            return false;
        }

        log.info("The payment with id={} is trusted", currentPayment.getId());

        return true;
    }
}
