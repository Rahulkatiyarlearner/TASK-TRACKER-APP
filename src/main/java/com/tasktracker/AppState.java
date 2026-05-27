package com.tasktracker;

import java.util.ArrayList;
import java.util.List;

public class AppState {
    private List<Task> tasks = new ArrayList<>();
    private List<Reminder> reminders = new ArrayList<>();

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders != null ? reminders : new ArrayList<>();
    }
}
