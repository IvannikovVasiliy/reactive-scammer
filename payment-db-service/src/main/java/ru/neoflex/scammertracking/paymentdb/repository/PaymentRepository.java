package ru.neoflex.scammertracking.paymentdb.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.paymentdb.domain.entity.PaymentEntity;

public interface PaymentRepository extends ReactiveCrudRepository<PaymentEntity, Long> {

    @Query("select * from payments where payer_card_number=$1 and date=(select max(date) from payments where payer_card_number=$1) LIMIT 1")
    Mono<PaymentEntity> findByPayerCardNumber(String cardNumber);

    @Query("insert INTO payments values (:#{#payment.id},:#{#payment.payerCardNumber},:#{#payment.receiverCardNumber},:#{#payment.latitude},:#{#payment.longitude},:#{#payment.date})")
    Mono<PaymentEntity> save(PaymentEntity payment);
}
