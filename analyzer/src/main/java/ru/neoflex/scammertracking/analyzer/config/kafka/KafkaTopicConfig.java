package ru.neoflex.scammertracking.analyzer.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import ru.neoflex.scammertracking.analyzer.log.CustomLogs;
import ru.neoflex.scammertracking.analyzer.util.ConfigUtil;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrapAddress}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        log.debug("configure kafkaAdmin-bean with bootstrap_servers={}", bootstrapServers);

        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topicPayments() {
        CustomLogs.logCreateTopicPayments();
        return TopicBuilder
                .name("payments")
                .partitions(ConfigUtil.getCountPartitionsTopicPayments())
                .replicas(ConfigUtil.getCountReplicasTopicPayments())
                .compact()
                .build();
    }

    @Bean
    public NewTopic topic2() {
        CustomLogs.logCreateTopicCheckedPayments();
        return TopicBuilder
                .name("checked-payments")
                .partitions(ConfigUtil.getCountPartitionsTopicCheckedPayments())
                .replicas(ConfigUtil.getCountReplicasTopicCheckedPayments())
                .compact()
                .build();
    }

    @Bean
    public NewTopic topic3() {
        CustomLogs.logCreateTopicSuspiciousPayments();
        return TopicBuilder.name("suspicious-payments")
                .partitions(ConfigUtil.getCountPartitionsTopicSuspiciousPayments())
                .replicas(ConfigUtil.getCountReplicasTopicSuspiciousPayments())
                .compact()
                .build();
    }
}
