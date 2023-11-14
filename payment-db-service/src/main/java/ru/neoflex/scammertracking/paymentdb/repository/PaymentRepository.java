package ru.neoflex.scammertracking.paymentdb.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.paymentdb.domain.entity.PaymentEntity;

public interface PaymentRepository extends ReactiveCrudRepository<PaymentEntity, Long> {

    @Query("select * from payments where payer_card_number=$1 and date=(select max(date) from payments where payer_card_number=$1) LIMIT 1")
    Mono<PaymentEntity> findByPayerCardNumber(String cardNumber);
//
    @Query("insert INTO payments values (:#{#payment.id},:#{#payment.payerCardNumber},:#{#payment.receiverCardNumber},:#{#payment.latitude},:#{#payment.longitude},:#{#payment.date})")
    Mono<Void> insert(PaymentEntity payment);

//    @Query("update payments set payer_card_number=:#{#payment.payerCardNumber},receiver_card_number=:#{#payment.receiverCardNumber},latitude=:#{#payment.latitude},longitude=:#{#payment.longitude},date=:#{#payment.date} where id=:#{#payment.id}")
//    Mono<PaymentEntity> update(PaymentEntity payment);

//    @Modifying
//    @Query("update payments set payer_card_number=:#{#payment.payerCardNumber},receiver_card_number=:#{#payment.receiverCardNumber},latitude=:#{#payment.latitude},longitude=:#{#payment.longitude},date=:#{#payment.date} where id=:#{#payment.id}")
//    Mono<Integer> update(PaymentEntity payment);
}
