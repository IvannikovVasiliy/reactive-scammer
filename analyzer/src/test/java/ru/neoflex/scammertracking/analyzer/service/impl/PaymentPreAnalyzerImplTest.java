package ru.neoflex.scammertracking.analyzer.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.neoflex.scammertracking.analyzer.config.AnalyzerConfig;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepositoryImpl;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.feign.FeignService;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.service.PaymentService;
import ru.neoflex.scammertracking.analyzer.utils.Constants;

import java.time.LocalDateTime;

import static org.mockito.Mockito.doThrow;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@SpringJUnitConfig({AnalyzerConfig.class})
class PaymentPreAnalyzerImplTest {

    @Mock
    private FeignService feignService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentCacheRepositoryImpl paymentCacheRepositoryImpl;
    @Mock
    private PaymentProducer paymentProducer;
//    @Mock
//    private ModelMapper modelMapper;
    @InjectMocks
    private PaymentPreAnalyzerImpl paymentAnalyzer;

    @BeforeEach
    public void init() {
        PaymentResponseDto paymentResponse = new PaymentResponseDto(1, Constants.PAYER_CARD_NUMBER, Constants.RECEIVER_CARD_NUMBER, Constants.COORDINATES, LocalDateTime.now(),false);
//        when(modelMapper.map(Mockito.any(), Mockito.eq(PaymentResponseDto.class)))
//                .thenReturn(paymentResponse);
    }

    @Test
    public void analyzeConsumeMessageTest() throws Exception {
        PaymentRequestDto paymentRequest = new PaymentRequestDto(Constants.ID, Constants.PAYER_CARD_NUMBER, Constants.RECEIVER_CARD_NUMBER, Constants.COORDINATES, LocalDateTime.now());
        LastPaymentResponseDto lastPaymentResponseDto = new LastPaymentResponseDto(1, Constants.PAYER_CARD_NUMBER, Constants.RECEIVER_CARD_NUMBER, Constants.COORDINATES, LocalDateTime.now());

//        when(paymentService.getLastPayment(Mockito.any(PaymentRequestDto.class), Mockito.any()))
//                .thenReturn(lastPaymentResponseDto);

        paymentAnalyzer.preAnalyzeConsumeMessage(String.valueOf(Constants.ID), paymentRequest);
    }

    @Test
    public void analyzeConsumeMessageGetLastPaymentErrorTest() throws Exception {
        PaymentRequestDto paymentRequest = new PaymentRequestDto(Constants.ID, Constants.PAYER_CARD_NUMBER, Constants.RECEIVER_CARD_NUMBER, Constants.COORDINATES, LocalDateTime.now());

//        when(paymentService.getLastPayment(Mockito.any(PaymentRequestDto.class), Mockito.any()))
//                .thenThrow(new NotFoundException("The payment not found"));

        paymentAnalyzer.preAnalyzeConsumeMessage(String.valueOf(Constants.ID), paymentRequest);
    }

    @Test
    public void checkSuspiciousTest() throws Exception {
        PaymentRequestDto paymentRequest1 = new PaymentRequestDto(Constants.ID, Constants.FAKE_CARD_NUMBER, Constants.RECEIVER_CARD_NUMBER, Constants.COORDINATES, LocalDateTime.now());
        PaymentRequestDto paymentRequest2 = new PaymentRequestDto(Constants.ID, Constants.PAYER_CARD_NUMBER, Constants.FAKE_CARD_NUMBER, Constants.COORDINATES, LocalDateTime.now());
        PaymentRequestDto paymentRequest3 = new PaymentRequestDto(Constants.ID, Constants.PAYER_CARD_NUMBER, Constants.RECEIVER_CARD_NUMBER, Constants.COORDINATES, Constants.FUTURE_DATETIME);

        paymentAnalyzer.preAnalyzeConsumeMessage(String.valueOf(Constants.ID), paymentRequest1);
        paymentAnalyzer.preAnalyzeConsumeMessage(String.valueOf(Constants.ID), paymentRequest2);
        paymentAnalyzer.preAnalyzeConsumeMessage(String.valueOf(Constants.ID), paymentRequest3);
    }

    @Test
    public void routePaymentErrorTest() throws Exception {
        PaymentRequestDto paymentRequest = new PaymentRequestDto(Constants.ID, Constants.PAYER_CARD_NUMBER, Constants.RECEIVER_CARD_NUMBER, Constants.COORDINATES, LocalDateTime.now());
        PaymentResponseDto paymentResponse = new PaymentResponseDto(1, Constants.PAYER_CARD_NUMBER, Constants.RECEIVER_CARD_NUMBER, Constants.COORDINATES, LocalDateTime.now(),false);
        LastPaymentResponseDto lastPaymentResponseDto = new LastPaymentResponseDto(1, Constants.PAYER_CARD_NUMBER, Constants.RECEIVER_CARD_NUMBER, Constants.COORDINATES, LocalDateTime.now());

//        when(paymentService.getLastPayment(Mockito.any(PaymentRequestDto.class), Mockito.any()))
//                .thenReturn(lastPaymentResponseDto);
//        doThrow(BadRequestException.class).when(feignService).savePayment(paymentRequest);

        paymentAnalyzer.preAnalyzeConsumeMessage(String.valueOf(Constants.ID), paymentRequest);
    }
}