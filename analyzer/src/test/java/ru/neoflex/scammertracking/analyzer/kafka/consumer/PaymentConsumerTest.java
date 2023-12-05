package ru.neoflex.scammertracking.analyzer.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.service.GetCachedPaymentRouter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.doThrow;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PaymentConsumerTest {

    @Autowired
    public PaymentConsumerTest(ObjectMapper objectMapper, PaymentConsumer paymentConsumer) {
        this.objectMapper = objectMapper;
    }

    @InjectMocks
    private PaymentConsumer paymentConsumer;
    @Mock
    private GetCachedPaymentRouter getCachedPaymentRouter;
    @Mock
    private PaymentProducer paymentProducer;
    @Mock
    private ObjectMapper objectMapperMock;

    private final ObjectMapper objectMapper;

    @Test
    void consumePaymentTest() throws IOException, URISyntaxException {
        URL url = getClass().getResource("/files/getPaymentRequestJson.json");
        String validPayment =
                FileUtils.readFileToString(new File(url.toURI()), StandardCharsets.UTF_8);
        PaymentRequestDto validPaymentRequestDto = objectMapper.readValue(validPayment, PaymentRequestDto.class);
        byte[] validPaymentRequestBytesDto = objectMapper.writeValueAsBytes(validPaymentRequestDto);

//        paymentConsumer.consumePayment(validPaymentRequestBytesDto, validPaymentRequestDto.getPayerCardNumber());
    }
}