package com.j3ly.spawnman.event;

import com.j3ly.spawnman.model.SpawnPoint;
import com.j3ly.spawnman.model.SpawnSet;
import com.j3ly.spawnman.storage.SpawnStorage;
import com.j3ly.spawnman.storage.TeamStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class PlayerSpawnHandler {
    private final SpawnStorage storage;
    private final TeamStorage teamStorage;

    public PlayerSpawnHandler(SpawnStorage storage, TeamStorage teamStorage) {
        this.storage = storage;
        this.teamStorage = teamStorage;
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        handleSpawn(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        handleSpawn(event.getEntity());
    }

    private void handleSpawn(Player player) {
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        String worldKey = player.level().dimension().location().toString();
        Collection<SpawnSet> worldSets = storage.getSpawnSetsForWorld(worldKey);

        if (worldSets.isEmpty()) return;

        String playerTeam = teamStorage.getPlayerTeam(player.getUUID());

        if (playerTeam != null) {
            for (SpawnSet set : worldSets) {
                if (playerTeam.equals(set.getTeam())) {
                    SpawnPoint point = set.getRandomSpawn();
                    if (point != null) {
                        serverPlayer.teleportTo(point.getX(), point.getY(), point.getZ());
                    }
                    return;
                }
            }
        }

        List<SpawnSet> nonTeamSets = worldSets.stream()
            .filter(s -> !s.hasTeam())
            .collect(Collectors.toList());

        if (nonTeamSets.isEmpty()) return;

        SpawnSet selected = nonTeamSets.get(ThreadLocalRandom.current().nextInt(nonTeamSets.size()));
        SpawnPoint point = selected.getRandomSpawn();

        if (point != null) {
            serverPlayer.teleportTo(point.getX(), point.getY(), point.getZ());
        }
    }
}