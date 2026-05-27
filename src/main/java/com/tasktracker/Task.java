package com.tasktracker;

import java.util.Objects;
import java.util.UUID;

public class Task {
    private String id;
    private String name;
    private long createdAtEpochMillis;
    /**
     * 0 means "not completed". Stored as epoch millis for persistence/sorting.
     */
    private long completedAtEpochMillis;

    public Task() {
        this.id = UUID.randomUUID().toString();
    }

    public Task(String name, long createdAtEpochMillis) {
        this();
        this.name = name;
        this.createdAtEpochMillis = createdAtEpochMillis;
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

    public long getCreatedAtEpochMillis() {
        return createdAtEpochMillis;
    }

    public void setCreatedAtEpochMillis(long createdAtEpochMillis) {
        this.createdAtEpochMillis = createdAtEpochMillis;
    }

    public long getCompletedAtEpochMillis() {
        return completedAtEpochMillis;
    }

    public void setCompletedAtEpochMillis(long completedAtEpochMillis) {
        this.completedAtEpochMillis = completedAtEpochMillis;
    }

    public boolean isCompleted() {
        return completedAtEpochMillis > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
