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
import ru.neoflex.scammertracking.analyzer.util.ConfigUtil;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${redis.hostName}")
    private String hostName;
    @Value("${redis.port}")
    private int port;

    private final Integer REDIS_TIMEOUT = ConfigUtil.getRedisTimeout();

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory lettuceConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
//                .commandTimeout(Duration.ofSeconds(REDIS_TIMEOUT))
//                .commandTimeout(Duration.ofMillis(1))
                .commandTimeout(Duration.ZERO)
                .shutdownTimeout(Duration.ZERO)
                .build();

        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(hostName, port), clientConfig);
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
        return new ReactiveRedisTemplate<>(factory, context);
    }

}