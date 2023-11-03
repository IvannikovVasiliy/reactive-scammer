package ru.neoflex.scammertracking.analyzer.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.*;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;

import java.time.Duration;

@Configuration
//@EnableRedisRepositories
public class RedisConfig {

    @Value("${redis.hostName}")
    private String hostName;
    @Value("${redis.port}")
    private int port;

//    @Bean
//    public JedisConnectionFactory connectionFactory() {
//        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
//        configuration.setHostName(hostName);
//        configuration.setPort(port);
//        return new JedisConnectionFactory(configuration);
//    }
//
//    @Bean
//    public RedisTemplate<String, Object> template() {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory());
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new JdkSerializationRedisSerializer());
//        template.setValueSerializer(new JdkSerializationRedisSerializer());
//        template.setEnableTransactionSupport(true);
//        template.afterPropertiesSet();
//        return template;
//    }

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory lettuceConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(2))
                .shutdownTimeout(Duration.ZERO)
                .build();

        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(hostName, port), clientConfig);
    }

    @Bean
    public ReactiveRedisTemplate<String, PaymentEntity> reactiveRedisTemplate(
            @Qualifier("lettuceConnectionFactory") ReactiveRedisConnectionFactory factory
    ) {
        Jackson2JsonRedisSerializer<PaymentEntity> serializer = new Jackson2JsonRedisSerializer<>(PaymentEntity.class);
//        RedisSerializationContext.RedisSerializationContextBuilder<String, PaymentEntity> builder =
//                RedisSerializationContext.
//                        newSerializationContext(new StringRedisSerializer());
//        RedisSerializationContext<String, PaymentEntity> context =
//                builder
//                        .value(serializer)
//                        .hashValue(new GenericJackson2JsonRedisSerializer())
//                        .build();
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