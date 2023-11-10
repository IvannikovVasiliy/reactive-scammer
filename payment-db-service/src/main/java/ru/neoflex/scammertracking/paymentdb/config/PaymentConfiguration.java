package ru.neoflex.scammertracking.paymentdb.config;

import io.r2dbc.pool.PoolingConnectionFactoryProvider;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLConnection;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfiguration {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
