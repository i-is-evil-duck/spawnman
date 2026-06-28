package com.ducky.mods.spawnman;

import com.ducky.mods.spawnman.command.SpawnManCommand;
import com.ducky.mods.spawnman.event.PlayerSpawnHandler;
import com.ducky.mods.spawnman.storage.SpawnStorage;
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

    private final SpawnStorage storage;

    public SpawnManMod() {
        this.storage = new SpawnStorage();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PlayerSpawnHandler(storage));
        LOGGER.info("SpawnMan loaded");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        new SpawnManCommand(storage).register(event.getDispatcher());
    }
}
