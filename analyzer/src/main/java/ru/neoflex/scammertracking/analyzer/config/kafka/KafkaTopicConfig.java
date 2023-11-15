package ru.neoflex.scammertracking.analyzer.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import ru.neoflex.scammertracking.analyzer.log.CustomLogs;
import ru.neoflex.scammertracking.analyzer.util.ConfigUtil;

@Configuration
@Slf4j
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentsTopic() {
        CustomLogs.logCreateTopicPayments();
        return TopicBuilder
                .name("payments")
                .partitions(ConfigUtil.getCountPartitionsTopicPayments())
                .replicas(ConfigUtil.getCountReplicasTopicPayments())
                .compact()
                .build();
    }

    @Bean
    public NewTopic checkedPaymentsTopic() {
        CustomLogs.logCreateTopicCheckedPayments();
        return TopicBuilder
                .name("checked-payments")
                .partitions(ConfigUtil.getCountPartitionsTopicCheckedPayments())
                .replicas(ConfigUtil.getCountReplicasTopicCheckedPayments())
                .compact()
                .build();
    }

    @Bean
    public NewTopic suspiciousPaymentsTopic() {
        CustomLogs.logCreateTopicSuspiciousPayments();
        return TopicBuilder
                .name("suspicious-payments")
                .partitions(ConfigUtil.getCountPartitionsTopicSuspiciousPayments())
                .replicas(ConfigUtil.getCountReplicasTopicSuspiciousPayments())
                .compact()
                .build();
    }
}
