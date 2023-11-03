package ru.neoflex.scammertracking.analyzer.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigUtil {

    private static Config config = ConfigFactory.load("conf/hostPort.conf");

    public static String getLastPaymentEndpoint() {
        return config.getString("lastPayment");
    }
    public static String savePaymentEndpoint() {
        return config.getString("savePayment");
    }
}
