package ru.neoflex.scammertracking.analyzer.check.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.model.Coordinates;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PaymentCheckerImplTest {

    @Autowired
    public PaymentCheckerImplTest(PaymentCheckerImpl checkRequest) {
        this.checkRequest = checkRequest;
        this.objectMapper = new ObjectMapper();
    }

    private final PaymentCheckerImpl checkRequest;
    private final ObjectMapper objectMapper;

    @Test
    public void preCheckSuspiciousTest() throws IOException, URISyntaxException {
        Coordinates coordinates = new Coordinates(50.0f, 50.0f);
        PaymentRequestDto invalidPayerCardNumberPaymentRequestDto =
                new PaymentRequestDto(1l, "123456", "1", coordinates, LocalDateTime.now());
        PaymentRequestDto invalidRecieverCardNumberPaymentRequestDto =
                new PaymentRequestDto(1l, "1", "123456", coordinates, LocalDateTime.now());
        PaymentRequestDto invalidDatePaymentRequestDto =
                new PaymentRequestDto(1l, "123456", "123456", coordinates, LocalDateTime.now().plusDays(1L));

        URL url = getClass().getResource("/files/getPaymentRequestJson.json");
        String validPayment =
                FileUtils.readFileToString(new File(url.toURI()), StandardCharsets.UTF_8);
        PaymentRequestDto validPaymentRequestDto = objectMapper.readValue(validPayment, PaymentRequestDto.class);

        boolean isPayerCardNumberPaymentSuspicious =
                checkRequest.preCheckSuspicious(invalidPayerCardNumberPaymentRequestDto);
        boolean isReceiverCardNumberPaymentSuspicious =
                checkRequest.preCheckSuspicious(invalidRecieverCardNumberPaymentRequestDto);
        boolean isDatePaymentSuspicious = checkRequest.preCheckSuspicious(invalidDatePaymentRequestDto);
        boolean isPaymentSuspicious = checkRequest.preCheckSuspicious(validPaymentRequestDto);

        assertTrue(isPayerCardNumberPaymentSuspicious);
        assertTrue(isReceiverCardNumberPaymentSuspicious);
        assertTrue(isDatePaymentSuspicious);
        assertFalse(isPaymentSuspicious);
    }
}