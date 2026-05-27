package com.tasktracker;

import java.awt.Toolkit;

public final class Sounds {

    private Sounds() {
    }

    public static void beepTimes(int count, int gapMs) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        for (int i = 0; i < count; i++) {
            tk.beep();
            if (i < count - 1 && gapMs > 0) {
                sleepQuiet(gapMs);
            }
        }
    }

    public static void taskNineHourAlert() {
        new Thread(() -> beepTimes(2, 180), "beep-9h").start();
    }

    public static void taskTwentyFourHourAlert() {
        new Thread(() -> beepTimes(4, 120), "beep-24h").start();
    }

    public static void reminderAlert() {
        new Thread(() -> beepTimes(3, 150), "beep-reminder").start();
    }

    private static void sleepQuiet(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
