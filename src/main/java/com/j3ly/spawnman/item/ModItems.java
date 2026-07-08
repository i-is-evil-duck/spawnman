package com.j3ly.spawnman.item;

import com.j3ly.spawnman.SpawnManMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SpawnManMod.MOD_ID);

    public static final RegistryObject<Item> POSITION_WAND = ITEMS.register("position_wand",
        PositionWandItem::new);
}
