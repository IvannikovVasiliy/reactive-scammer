package ru.neoflex.scammertracking.analyzer.util;

public class Constants {

    public static final Integer PAYMENT_ALREADY_EXISTS_ERROR_CODE = -2;

    public static final long SCHEDULING_POLL_MESSAGES_INTERVAL = 500;
    public static final long SCHEDULING_ITERATE_BACKOFF_PAYMENTS_INTERVAL = 10_000;
    public static final long SCHEDULING_EXPIRATION_CACHE_INTERVAL = 60_000;
    public static final long SCHEDULING_REDIS_START = 300_000;

    public static final String CORRELATION_ID_HEADER_NAME = "Correlation-Id";

    public static final long SUBSCRIPTION_REQUEST_COUNT = 100;
}