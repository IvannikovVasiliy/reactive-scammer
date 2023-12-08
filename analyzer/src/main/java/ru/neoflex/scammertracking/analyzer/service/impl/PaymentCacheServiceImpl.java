package ru.neoflex.scammertracking.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.service.PaymentCacheService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCacheServiceImpl implements PaymentCacheService {

    private final PaymentCacheRepository paymentCacheRepository;
    private final SourceMapperImplementation sourceMapper;

    @Override
    public void saveIfAbsent(SavePaymentResponseDto savePaymentRequest) {
        paymentCacheRepository
                .findPaymentByCardNumber(savePaymentRequest.getPayerCardNumber())
                .subscribe(new BaseSubscriber<>() {

                    PaymentEntity payment = null;

                    @Override
                    protected void hookOnNext(PaymentEntity payment) {
                        super.hookOnNext(payment);
                        log.info("hookOnNext. get payment from cache. payment={ payerCardNumber={}, receiverCardNumber={}, idPayment={}, latitude={}, longitude={}, datePayment={} }",
                                payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getIdPayment(), payment.getLatitude(), payment.getLongitude(), payment.getDatePayment());
                        this.payment = payment;
                    }

                    @Override
                    protected void hookOnComplete() {
                        super.hookOnComplete();
                        log.info("hookOnComplete. Complete getting cache payment");
                        if (payment == null) {
                            PaymentEntity paymentEntity =
                                    sourceMapper.sourceFromSavePaymentResponseDtoToPaymentEntity(savePaymentRequest);
                            paymentCacheRepository
                                    .save(paymentEntity)
                                    .subscribe();
                        }
                    }
                });
    }
}
