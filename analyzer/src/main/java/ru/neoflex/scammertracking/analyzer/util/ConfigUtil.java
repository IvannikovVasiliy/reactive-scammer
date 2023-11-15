package ru.neoflex.scammertracking.analyzer.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigUtil {

    private static final Config config = ConfigFactory.load("conf/appConfig.conf");

    public static String getLastPaymentEndpoint() {
        return config.getString("lastPayment");
    }
    public static String savePaymentEndpoint() {
        return config.getString("savePayment");
    }
    public static Integer getRedisTimeout() {
        return config.getInt("redisTemplate");
    }

    public static Integer getCountPartitionsTopicPayments() {
        return config.getInt("countPartitionsTopicPayments");
    }
    public static Integer getCountPartitionsTopicCheckedPayments() {
        return config.getInt("countPartitionsTopicCheckedPayments");
    }
    public static Integer getCountPartitionsTopicSuspiciousPayments() {
        return config.getInt("countPartitionsTopicSuspiciousPayments");
    }

    public static Integer getCountReplicasTopicPayments() {
        return config.getInt("countReplicasTopicPayments");
    }
    public static Integer getCountReplicasTopicCheckedPayments() {
        return config.getInt("countReplicasTopicCheckedPayments");
    }
    public static Integer getCountReplicasTopicSuspiciousPayments() {
        return config.getInt("countReplicasTopicSuspiciousPayments");
    }
}
