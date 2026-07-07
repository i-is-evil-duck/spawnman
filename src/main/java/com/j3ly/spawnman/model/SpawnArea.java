package com.j3ly.spawnman.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnArea {
    private double x1, y1, z1;
    private double x2, y2, z2;

    public SpawnArea(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.x1 = Math.min(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.y2 = Math.max(y1, y2);
        this.z2 = Math.max(z1, z2);
    }

    public SpawnPoint getRandomPoint() {
        double x = ThreadLocalRandom.current().nextDouble(x1, x2 + 1);
        double y = ThreadLocalRandom.current().nextDouble(y1, y2 + 1);
        double z = ThreadLocalRandom.current().nextDouble(z1, z2 + 1);
        return new SpawnPoint(Math.floor(x) + 0.5, y, Math.floor(z) + 0.5);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("x1", x1);
        data.put("y1", y1);
        data.put("z1", z1);
        data.put("x2", x2);
        data.put("y2", y2);
        data.put("z2", z2);
        return data;
    }

    public static SpawnArea deserialize(Map<String, Object> data) {
        return new SpawnArea(
            ((Number) data.get("x1")).doubleValue(),
            ((Number) data.get("y1")).doubleValue(),
            ((Number) data.get("z1")).doubleValue(),
            ((Number) data.get("x2")).doubleValue(),
            ((Number) data.get("y2")).doubleValue(),
            ((Number) data.get("z2")).doubleValue()
        );
    }
}
