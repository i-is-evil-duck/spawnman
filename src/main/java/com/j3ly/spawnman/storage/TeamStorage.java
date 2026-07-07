package com.j3ly.spawnman.storage;

import com.google.gson.*;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeamStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path teamFile;
    private final Map<UUID, String> playerTeams = new ConcurrentHashMap<>();

    public TeamStorage() {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve("spawnman");
        this.teamFile = configDir.resolve("teams.json");
        load();
    }

    public void setPlayerTeam(UUID playerUuid, String team) {
        if (team == null || team.isEmpty()) {
            playerTeams.remove(playerUuid);
        } else {
            playerTeams.put(playerUuid, team);
        }
        save();
    }

    public String getPlayerTeam(UUID playerUuid) {
        return playerTeams.get(playerUuid);
    }

    public boolean hasTeam(UUID playerUuid) {
        return playerTeams.containsKey(playerUuid);
    }

    public void removePlayer(UUID playerUuid) {
        playerTeams.remove(playerUuid);
        save();
    }

    public Collection<String> getAllTeams() {
        return new HashSet<>(playerTeams.values());
    }

    private void save() {
        try {
            Files.createDirectories(teamFile.getParent());
            JsonObject root = new JsonObject();
            for (Map.Entry<UUID, String> entry : playerTeams.entrySet()) {
                root.addProperty(entry.getKey().toString(), entry.getValue());
            }
            Files.writeString(teamFile, GSON.toJson(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        playerTeams.clear();
        if (!Files.exists(teamFile)) return;
        try {
            String content = Files.readString(teamFile);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();
            for (String key : root.keySet()) {
                playerTeams.put(UUID.fromString(key), root.get(key).getAsString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}