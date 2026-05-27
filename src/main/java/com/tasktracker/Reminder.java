package com.tasktracker;

import java.time.DayOfWeek;
import java.util.Objects;
import java.util.UUID;

public class Reminder {
    public enum Recurrence {
        DAILY,
        WEEKLY,
        MONTHLY
    }

    private String id;
    private String name;
    private int hour24;
    private int minute;
    private Recurrence recurrence;
    /** For WEEKLY: DayOfWeek.getValue() 1=Monday .. 7=Sunday */
    private int dayOfWeekValue;
    /** For MONTHLY: 1-31 */
    private int dayOfMonth;
    private long lastFiredEpochMinute;

    public Reminder() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHour24() {
        return hour24;
    }

    public void setHour24(int hour24) {
        this.hour24 = hour24;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public Recurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }

    public int getDayOfWeekValue() {
        return dayOfWeekValue;
    }

    public void setDayOfWeekValue(int dayOfWeekValue) {
        this.dayOfWeekValue = dayOfWeekValue;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public long getLastFiredEpochMinute() {
        return lastFiredEpochMinute;
    }

    public void setLastFiredEpochMinute(long lastFiredEpochMinute) {
        this.lastFiredEpochMinute = lastFiredEpochMinute;
    }

    public static String dayOfWeekLabel(int value) {
        if (value < 1 || value > 7) return "?";
        return DayOfWeek.of(value).name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return Objects.equals(id, reminder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
