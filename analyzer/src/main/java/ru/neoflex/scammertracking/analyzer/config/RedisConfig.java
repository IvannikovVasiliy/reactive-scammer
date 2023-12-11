package ru.neoflex.scammertracking.analyzer.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
public class RedisConfig {

    @Value("${redis.hostName}")
    private String HOST_NAME;
    @Value("${redis.port}")
    private int PORT;

    @Value("${app.redisTimeout}")
    private Integer REDIS_TIMEOUT;

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory lettuceConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration
                .builder()
                .commandTimeout(Duration.ofMillis(5))
                .shutdownTimeout(Duration.ofMillis(5))
                .build();

        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(HOST_NAME, PORT), clientConfig);
    }

    @Bean
    public ReactiveRedisTemplate<String, PaymentEntity> reactiveRedisTemplate(
            @Qualifier("lettuceConnectionFactory") ReactiveRedisConnectionFactory factory
    ) {
        Jackson2JsonRedisSerializer<PaymentEntity> serializer = new Jackson2JsonRedisSerializer<>(PaymentEntity.class);
        RedisSerializationContext<String, PaymentEntity> context = RedisSerializationContext
                .<String, PaymentEntity>newSerializationContext(new StringRedisSerializer())
                .key(new StringRedisSerializer())
                .value(serializer)
                .hashKey(new Jackson2JsonRedisSerializer<>(String.class))
                .hashValue(new GenericJackson2JsonRedisSerializer())
                .build();

        ReactiveRedisTemplate<String, PaymentEntity> reactiveRedisTemplate = new ReactiveRedisTemplate<>(factory, context);
        reactiveRedisTemplate.expire("Payment", Duration.of(5, ChronoUnit.SECONDS)).subscribe();

        return reactiveRedisTemplate;
    }

}