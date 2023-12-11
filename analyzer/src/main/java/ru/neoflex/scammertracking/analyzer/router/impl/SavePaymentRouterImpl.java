package ru.neoflex.scammertracking.analyzer.router.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.PrematureCloseException;
import reactor.util.retry.Retry;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.MessageInfoDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.model.Violation;
import ru.neoflex.scammertracking.analyzer.domain.model.WrapPaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.exception.ConnectionRefusedException;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.router.SavePaymentRouter;
import ru.neoflex.scammertracking.analyzer.service.PaymentCacheService;
import ru.neoflex.scammertracking.analyzer.util.Constants;

import java.net.ConnectException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavePaymentRouterImpl implements SavePaymentRouter {

    private final ClientService clientService;
    private final PaymentCacheService paymentCacheService;
    private final AtomicBoolean isRedisDropped;
    private final Map<Long, WrapPaymentRequestDto> storage;
    private final ObjectMapper objectMapper;
    private final PaymentProducer paymentProducer;

    @Value("${app.retryCount}")
    private Long RETRY_COUNT;
    @Value("${app.retryIntervalSeconds}")
    private Long RETRY_INTERVAL_SECONDS;

    public void savePayment(Flux<SavePaymentRequestDto> savePaymentDtoFlux) {
        clientService
                .savePayment(savePaymentDtoFlux)
                .doOnNext(payment -> {
                    storage.remove(payment.getId());
                    if (!isRedisDropped.get()) {
                        paymentCacheService.saveIfAbsent(payment);
                    }
//                    paymentProducer.sendCheckedMessage(payment);
                })
                .retryWhen(Retry
                        .fixedDelay(RETRY_COUNT, Duration.ofSeconds(RETRY_INTERVAL_SECONDS))
                        .filter(throwable ->
                                throwable.getCause() instanceof ConnectException ||
                                        throwable.getCause() instanceof PrematureCloseException)
                        .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {
                            throw new ConnectionRefusedException("Error getLastPaymentFromClientService. External ms-payment failed to process after max retries");
                        })))
                .subscribe(new BaseSubscriber<>() {

                    Subscription subscription;
                    final AtomicInteger count = new AtomicInteger();

                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        this.subscription = subscription;
                        subscription.request(Constants.SUBSCRIPTION_REQUEST_COUNT);
                    }

                    @Override
                    protected void hookOnNext(SavePaymentResponseDto value) {
                        super.hookOnNext(value);
                        if (count.incrementAndGet() == Constants.SUBSCRIPTION_REQUEST_COUNT) {
                            count.set(0);
                            subscription.request(Constants.SUBSCRIPTION_REQUEST_COUNT);
                        }
                    }

                    @Override
                    protected void hookOnError(Throwable err) {
                        if (count.incrementAndGet() == Constants.SUBSCRIPTION_REQUEST_COUNT) {
                            count.set(0);
                            subscription.request(Constants.SUBSCRIPTION_REQUEST_COUNT);
                        }

                        List<String> correlationIdList = ((WebClientResponseException.BadRequest) err)
                                .getHeaders()
                                .get(Constants.CORRELATION_ID_HEADER_NAME);

                        try {
                            List<Violation> violations = objectMapper.readValue(((WebClientResponseException.BadRequest) err).getResponseBodyAs(byte[].class), List.class);
                            for (String correlationId : correlationIdList) {
                                PaymentRequestDto paymentRequestDto = storage.get(Long.valueOf(correlationId)).getPaymentRequestDto();
                                byte[] paymentRequestBytes = objectMapper.writeValueAsBytes(paymentRequestDto);
                                paymentProducer.sendSuspiciousMessage(paymentRequestDto.getId().toString(), paymentRequestBytes);
                            }
                        } catch (Exception e) { }

                        try {
                            MessageInfoDto messageInfoDto = objectMapper.readValue(
                                    ((WebClientResponseException.BadRequest) err).getResponseBodyAs(byte[].class),
                                    MessageInfoDto.class
                            );
                            if (Constants.PAYMENT_ALREADY_EXISTS_ERROR_CODE.equals(messageInfoDto.getErrorCode())) {
                                for (String correlationId : correlationIdList) {
                                    PaymentRequestDto paymentRequestDto = storage.get(Long.valueOf(correlationId)).getPaymentRequestDto();
                                    byte[] paymentRequestBytes = objectMapper.writeValueAsBytes(paymentRequestDto);
                                    paymentProducer.sendSuspiciousMessage(paymentRequestDto.getId().toString(), paymentRequestBytes);
                                }
                            }
                        } catch (Exception e) {}
                    }
                });
    }
}
