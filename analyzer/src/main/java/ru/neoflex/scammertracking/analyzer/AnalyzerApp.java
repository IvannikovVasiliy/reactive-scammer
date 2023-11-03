package ru.neoflex.scammertracking.analyzer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.publisher.Flux;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;

import java.util.UUID;

@SpringBootApplication
@RequiredArgsConstructor
//@EnableFeignClients
public class AnalyzerApp {

    public static void main(String[] args) {
        SpringApplication.run(AnalyzerApp.class, args);
    }

//    private final ReactiveRedisConnectionFactory factory;
//    private final ReactiveRedisOperations<String, PaymentEntity> coffeeOps;
//    private final ReactiveRedisTemplate<String, PaymentEntity> redisTemplate;
//
//
//    @PostConstruct
//    public void loadData() {
////        factory.getReactiveConnection().serverCommands().flushAll().thenMany(
////                        Flux.just("Jet Black Redis", "Darth Redis", "Black Alert Redis")
////                                .map(name -> new PaymentEntity())
////                                .flatMap(coffee -> coffeeOps.opsForValue().set("coffee", coffee)))
////                .thenMany(coffeeOps.keys("*")
////                        .flatMap(coffeeOps.opsForValue()::get))
////                .subscribe(System.out::println);
//        PaymentEntity paymentEntity = new PaymentEntity();
//        paymentEntity.setPayerCardNumber("1");
//        redisTemplate.opsForHash().put("Payment", "1", paymentEntity).subscribe();
//        coffeeOps.opsForHash().put("ds", "1", paymentEntity).subscribe();
//    }
}
