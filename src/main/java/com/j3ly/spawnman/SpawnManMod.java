package com.j3ly.spawnman;

import com.j3ly.spawnman.command.SpawnManCommand;
import com.j3ly.spawnman.command.TeamCommand;
import com.j3ly.spawnman.event.PlayerSpawnHandler;
import com.j3ly.spawnman.storage.SpawnStorage;
import com.j3ly.spawnman.storage.TeamStorage;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(SpawnManMod.MOD_ID)
public class SpawnManMod {
    public static final String MOD_ID = "spawnman";
    public static final Logger LOGGER = LogUtils.getLogger();

    private final SpawnStorage spawnStorage;
    private final TeamStorage teamStorage;

    public SpawnManMod() {
        this.spawnStorage = new SpawnStorage();
        this.teamStorage = new TeamStorage();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PlayerSpawnHandler(spawnStorage, teamStorage));
        LOGGER.info("SpawnMan loaded");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        new SpawnManCommand(spawnStorage).register(event.getDispatcher());
        new TeamCommand(spawnStorage, teamStorage).register(event.getDispatcher());
    }
}