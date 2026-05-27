package com.tasktracker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class ReminderScheduler {

    private ReminderScheduler() {
    }

    /**
     * Returns true if this wall-clock minute should trigger the reminder (once per matching minute).
     */
    public static boolean matchesThisMinute(Reminder r, ZonedDateTime nowZ) {
        LocalDateTime now = nowZ.toLocalDateTime().withSecond(0).withNano(0);
        if (now.getHour() != r.getHour24() || now.getMinute() != r.getMinute()) {
            return false;
        }
        LocalDate today = now.toLocalDate();
        switch (r.getRecurrence()) {
            case DAILY:
                return true;
            case WEEKLY:
                return now.getDayOfWeek().getValue() == r.getDayOfWeekValue();
            case MONTHLY:
                int dom = Math.min(r.getDayOfMonth(), YearMonth.from(today).lengthOfMonth());
                return today.getDayOfMonth() == dom;
            default:
                return false;
        }
    }

    public static LocalTime timeOf(Reminder r) {
        return LocalTime.of(r.getHour24(), r.getMinute());
    }

    public static ZoneId systemZone() {
        return ZoneId.systemDefault();
    }
}
