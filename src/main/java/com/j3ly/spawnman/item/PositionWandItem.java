package com.j3ly.spawnman.item;

import com.j3ly.spawnman.command.SpawnManCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

public class PositionWandItem extends Item {
    public PositionWandItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null || !player.getAbilities().instabuild && !player.hasPermissions(2)) {
            return InteractionResult.PASS;
        }
        BlockPos pos = ctx.getClickedPos();
        Vec3 spawnPos = new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        setMarker(player, 1, spawnPos);
        player.displayClientMessage(Component.literal(
            String.format("§aPos 1 set to: §e%.1f, %.1f, %.1f", spawnPos.x, spawnPos.y, spawnPos.z)), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.getAbilities().instabuild && !player.hasPermissions(2)) return false;
        Vec3 pos = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        setMarker(player, 2, pos);
        player.displayClientMessage(Component.literal(
            String.format("§aPos 2 set to: §e%.1f, %.1f, %.1f", pos.x, pos.y, pos.z)), true);
        return true;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (player.level().isClientSide) return false;
        if (!player.getAbilities().instabuild && !player.hasPermissions(2)) return false;
        Vec3 spawnPos = new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        setMarker(player, 2, spawnPos);
        player.displayClientMessage(Component.literal(
            String.format("§aPos 2 set to: §e%.1f, %.1f, %.1f", spawnPos.x, spawnPos.y, spawnPos.z)), true);
        return true;
    }

    private static void setMarker(Player player, int num, Vec3 pos) {
        SpawnManCommand.playerMarkers.computeIfAbsent(player.getUUID(), k -> new Vec3[11])[num] = pos;
    }
}
