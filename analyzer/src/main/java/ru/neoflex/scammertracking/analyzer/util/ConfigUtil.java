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
}
