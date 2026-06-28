package com.ducky.mods.spawnman.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpawnPoint {
    private double x, y, z;

    public SpawnPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("x", x);
        data.put("y", y);
        data.put("z", z);
        return data;
    }

    public static SpawnPoint deserialize(Map<String, Object> data) {
        return new SpawnPoint(
            ((Number) data.get("x")).doubleValue(),
            ((Number) data.get("y")).doubleValue(),
            ((Number) data.get("z")).doubleValue()
        );
    }
}
