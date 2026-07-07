package com.j3ly.spawnman.model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnSet {
    private String id;
    private final List<SpawnPoint> points = new ArrayList<>();
    private SpawnArea area;
    private String world;
    private String team;

    public SpawnSet(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<SpawnPoint> getPoints() { return points; }

    public SpawnArea getArea() { return area; }
    public void setArea(SpawnArea area) { this.area = area; }

    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public boolean isArea() { return area != null; }

    public boolean hasTeam() { return team != null && !team.isEmpty(); }

    public SpawnPoint getRandomSpawn() {
        if (isArea()) {
            return area.getRandomPoint();
        }
        if (points.isEmpty()) return null;
        return points.get(ThreadLocalRandom.current().nextInt(points.size()));
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("world", world);
        data.put("team", team);

        List<Map<String, Object>> pointList = new ArrayList<>();
        for (SpawnPoint p : points) {
            pointList.add(p.serialize());
        }
        data.put("points", pointList);

        if (area != null) {
            data.put("area", area.serialize());
        }

        return data;
    }

    @SuppressWarnings("unchecked")
    public static SpawnSet deserialize(Map<String, Object> data) {
        SpawnSet set = new SpawnSet((String) data.get("id"));
        set.world = (String) data.get("world");
        set.team = (String) data.get("team");

        Object pointsObj = data.get("points");
        if (pointsObj instanceof List) {
            for (Map<String, Object> pData : (List<Map<String, Object>>) pointsObj) {
                set.points.add(SpawnPoint.deserialize(pData));
            }
        }

        Object areaObj = data.get("area");
        if (areaObj instanceof Map) {
            set.area = SpawnArea.deserialize((Map<String, Object>) areaObj);
        }

        return set;
    }
}
