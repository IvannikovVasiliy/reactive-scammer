package ru.neoflex.scammertracking.analyzer.util;

public class Constants {

    public static final Integer NOT_FOUND = 404;
    public static final Integer BAD_REQUEST = 400;
    public static final Integer RETRY_COUNT = 3;
    public static final Integer RETRY_INTERVAL = 3;

    public static final long SCHEDULING_POLL_MESSAGES_INTERVAL = 500;
    public static final long SCHEDULING_ITERATE_BACKOFF_PAYMENTS_INTERVAL = 10_000;
    public static final long SCHEDULING_EXPIRATION_CACHE_INTERVAL = 30_000;
    public static final long DELAY_REDIS_RESPONSE_MILLIS = 5;

    public static final String GROUP_BOUNDED_ELASTIC_FOR_CACHE = "SAVE_GROUP_BOUNDED_ELASTIC_FOR_CACHE";
}
