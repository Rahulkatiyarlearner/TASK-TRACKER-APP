package com.tasktracker;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class TaskElapsed {

    public enum VisualState {
        UNDER_NINE_HOURS,
        NINE_TO_TWENTY_FOUR,
        OVER_TWENTY_FOUR
    }

    private static final DateTimeFormatter CREATED_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").withZone(ZoneId.systemDefault());

    private TaskElapsed() {
    }

    public static VisualState visualState(long createdAtEpochMillis) {
        long elapsedMs = System.currentTimeMillis() - createdAtEpochMillis;
        double hours = elapsedMs / 3_600_000.0;
        if (hours < 9) {
            return VisualState.UNDER_NINE_HOURS;
        }
        if (hours < 24) {
            return VisualState.NINE_TO_TWENTY_FOUR;
        }
        return VisualState.OVER_TWENTY_FOUR;
    }

    public static String formatCreated(long createdAtEpochMillis) {
        return CREATED_FORMAT.format(Instant.ofEpochMilli(createdAtEpochMillis));
    }

    public static String formatElapsedRunning(long createdAtEpochMillis) {
        Duration d = Duration.between(
                Instant.ofEpochMilli(createdAtEpochMillis),
                Instant.now());
        if (d.isNegative()) {
            d = Duration.ZERO;
        }
        long days = d.toDays();
        long hours = d.toHours() % 24;
        long minutes = d.toMinutes() % 60;
        long seconds = d.getSeconds() % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(days == 1 ? " day " : " days ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append(hours == 1 ? " hour " : " hours ");
        }
        sb.append(minutes).append(minutes == 1 ? " minute " : " minutes ");
        sb.append(seconds).append(seconds == 1 ? " second" : " seconds");
        sb.append(" passed");
        return sb.toString();
    }

    public static ZonedDateTime nowLocal() {
        return ZonedDateTime.now(ZoneId.systemDefault());
    }
}
