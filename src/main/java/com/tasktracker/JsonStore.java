package com.tasktracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file;

    public JsonStore() {
        Path dir = Paths.get(System.getProperty("user.home"), ".task-tracker-app");
        this.file = dir.resolve("state.json");
    }

    public Path getFile() {
        return file;
    }

    public AppState load() {
        try {
            if (!Files.isRegularFile(file)) {
                return new AppState();
            }
            String json = Files.readString(file, StandardCharsets.UTF_8);
            AppState state = GSON.fromJson(json, AppState.class);
            return state != null ? state : new AppState();
        } catch (Exception e) {
            return new AppState();
        }
    }

    public void save(AppState state) throws IOException {
        Files.createDirectories(file.getParent());
        String json = GSON.toJson(state);
        Path tmp = file.resolveSibling(file.getFileName().toString() + ".tmp");
        Files.writeString(tmp, json, StandardCharsets.UTF_8);
        try {
            Files.move(tmp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(tmp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
