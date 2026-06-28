package com.ducky.mods.spawnman.storage;

import com.google.gson.*;
import com.ducky.mods.spawnman.model.SpawnSet;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path spawnFile;
    private final Map<String, SpawnSet> spawnSets = new ConcurrentHashMap<>();

    public SpawnStorage() {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve("spawnman");
        this.spawnFile = configDir.resolve("spawnsets.json");
        load();
    }

    public Collection<SpawnSet> getAllSpawnSets() {
        return spawnSets.values();
    }

    public SpawnSet getSpawnSet(String id) {
        return spawnSets.get(id);
    }

    public void addSpawnSet(SpawnSet set) {
        spawnSets.put(set.getId(), set);
        save();
    }

    public void removeSpawnSet(String id) {
        spawnSets.remove(id);
        save();
    }

    public boolean hasSpawnSet(String id) {
        return spawnSets.containsKey(id);
    }

    public Collection<SpawnSet> getSpawnSetsForWorld(String world) {
        List<SpawnSet> result = new ArrayList<>();
        for (SpawnSet set : spawnSets.values()) {
            if (world.equals(set.getWorld())) {
                result.add(set);
            }
        }
        return result;
    }

    private void save() {
        try {
            Files.createDirectories(spawnFile.getParent());
            List<Map<String, Object>> list = new ArrayList<>();
            for (SpawnSet set : spawnSets.values()) {
                list.add(set.serialize());
            }
            Files.writeString(spawnFile, GSON.toJson(list));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        spawnSets.clear();
        if (!Files.exists(spawnFile)) return;
        try {
            String content = Files.readString(spawnFile);
            JsonArray arr = JsonParser.parseString(content).getAsJsonArray();
            for (JsonElement el : arr) {
                Map<String, Object> map = jsonToMap(el.getAsJsonObject());
                SpawnSet set = SpawnSet.deserialize(map);
                spawnSets.put(set.getId(), set);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> jsonToMap(JsonObject obj) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : obj.keySet()) {
            JsonElement el = obj.get(key);
            if (el.isJsonPrimitive()) {
                JsonPrimitive p = el.getAsJsonPrimitive();
                if (p.isString()) map.put(key, p.getAsString());
                else if (p.isNumber()) map.put(key, p.getAsNumber());
                else if (p.isBoolean()) map.put(key, p.getAsBoolean());
            } else if (el.isJsonArray()) {
                List<Object> list = new ArrayList<>();
                for (JsonElement arrEl : el.getAsJsonArray()) {
                    if (arrEl.isJsonObject()) {
                        list.add(jsonToMap(arrEl.getAsJsonObject()));
                    } else if (arrEl.isJsonPrimitive()) {
                        list.add(arrEl.getAsString());
                    }
                }
                map.put(key, list);
            } else if (el.isJsonObject()) {
                map.put(key, jsonToMap(el.getAsJsonObject()));
            }
        }
        return map;
    }
}
