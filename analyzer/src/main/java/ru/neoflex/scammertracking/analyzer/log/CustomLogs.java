package ru.neoflex.scammertracking.analyzer.log;

import lombok.extern.slf4j.Slf4j;
import ru.neoflex.scammertracking.analyzer.util.ConfigUtil;

@Slf4j
public class CustomLogs {

    public static void logCreateTopicPayments() {
        log.debug("create topic payments with partitions-count={} and replicas-count={}", ConfigUtil.getCountPartitionsTopicPayments(), ConfigUtil.getCountReplicasTopicPayments());
    }

    public static void logCreateTopicCheckedPayments() {
        log.debug("create topic checked-payments with partitions-count={} and replicas-count={}", ConfigUtil.getCountPartitionsTopicCheckedPayments(), ConfigUtil.getCountReplicasTopicCheckedPayments());
    }

    public static void logCreateTopicSuspiciousPayments() {
        log.debug("create topic suspicious-payments with partitions-count={} and replicas-count={}", ConfigUtil.getCountPartitionsTopicSuspiciousPayments(), ConfigUtil.getCountReplicasTopicSuspiciousPayments());
    }
}
