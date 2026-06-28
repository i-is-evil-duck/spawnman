package com.ducky.mods.spawnman.event;

import com.ducky.mods.spawnman.model.SpawnPoint;
import com.ducky.mods.spawnman.model.SpawnSet;
import com.ducky.mods.spawnman.storage.SpawnStorage;
import net.minecraft.resources.ResourceLocation;
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

    public PlayerSpawnHandler(SpawnStorage storage) {
        this.storage = storage;
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

        List<SpawnSet> sets = worldSets.stream().collect(Collectors.toList());
        SpawnSet selected = sets.get(ThreadLocalRandom.current().nextInt(sets.size()));
        SpawnPoint point = selected.getRandomSpawn();

        if (point != null) {
            serverPlayer.teleportTo(point.getX(), point.getY(), point.getZ());
        }
    }
}
