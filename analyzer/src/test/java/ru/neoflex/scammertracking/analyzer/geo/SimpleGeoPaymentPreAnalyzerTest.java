package ru.neoflex.scammertracking.analyzer.geo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.model.Coordinates;
import ru.neoflex.scammertracking.analyzer.utils.Constants;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class SimpleGeoPaymentPreAnalyzerTest {

    @Autowired
    public SimpleGeoPaymentPreAnalyzerTest(SimpleGeoPaymentAnalyzer geoPaymentAnalyzer) {
        this.geoPaymentAnalyzer = geoPaymentAnalyzer;
    }

    private final SimpleGeoPaymentAnalyzer geoPaymentAnalyzer;

    @Test
    public void checkPaymentTest() {
        final long ID_1 = 1;
        final long ID_2 = 2;
        Coordinates coordinates1 = new Coordinates(Constants.TEST_COORDINATE_2, Constants.TEST_COORDINATE_1);
        Coordinates coordinates2 = new Coordinates(Constants.TEST_COORDINATE_3, Constants.TEST_COORDINATE_3);
        LocalDateTime lastPaymentDatetime1 = LocalDateTime.now().minusMinutes(59);
        LocalDateTime lastPaymentDatetime2 = LocalDateTime.now().minusHours(2);
        LocalDateTime currentPaymentDatetime = LocalDateTime.now();

        LastPaymentResponseDto lastPayment1 = new LastPaymentResponseDto(ID_1, Constants.PAYER_CARD_NUMBER, Constants.RECEIVER_CARD_NUMBER, coordinates1, lastPaymentDatetime1);
        LastPaymentResponseDto lastPayment2 = new LastPaymentResponseDto(ID_1, Constants.PAYER_CARD_NUMBER, Constants.RECEIVER_CARD_NUMBER, coordinates1, lastPaymentDatetime2);
        PaymentRequestDto currentPayment = new PaymentRequestDto(ID_2, Constants.PAYER_CARD_NUMBER, Constants.RECEIVER_CARD_NUMBER, coordinates2, currentPaymentDatetime);

        boolean isTrusted1 = geoPaymentAnalyzer.checkPayment(lastPayment1, currentPayment);
        boolean isTrusted2 = geoPaymentAnalyzer.checkPayment(lastPayment2, currentPayment);

        assertFalse(isTrusted1);
        assertTrue(isTrusted2);
    }

}