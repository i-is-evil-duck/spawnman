package com.j3ly.spawnman.command;

import com.j3ly.spawnman.event.PlayerSpawnHandler;
import com.j3ly.spawnman.model.SpawnPoint;
import com.j3ly.spawnman.model.SpawnSet;
import com.j3ly.spawnman.storage.SpawnStorage;
import com.j3ly.spawnman.storage.TeamStorage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

public class TeamCommand {
    private final SpawnStorage spawnStorage;
    private final TeamStorage teamStorage;

    public TeamCommand(SpawnStorage spawnStorage, TeamStorage teamStorage) {
        this.spawnStorage = spawnStorage;
        this.teamStorage = teamStorage;
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("team")
                .then(Commands.literal("join")
                    .then(Commands.argument("team", StringArgumentType.word())
                        .executes(this::joinTeam)))
                .then(Commands.literal("leave")
                    .executes(this::leaveTeam))
        );
    }

    private int joinTeam(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("§cMust be used by a player"));
            return 0;
        }

        String team = StringArgumentType.getString(ctx, "team");
        String worldKey = player.level().dimension().location().toString();

        Collection<SpawnSet> worldSets = spawnStorage.getSpawnSetsForWorld(worldKey);
        SpawnSet teamSpawn = null;
        for (SpawnSet set : worldSets) {
            if (team.equals(set.getTeam())) {
                teamSpawn = set;
                break;
            }
        }

        teamStorage.setPlayerTeam(player.getUUID(), team);

        if (teamSpawn != null) {
            SpawnPoint point = teamSpawn.getRandomSpawn();
            if (point != null) {
                Vec3 safe = PlayerSpawnHandler.findSafeSpawn(player.level(), point.getX(), point.getY(), point.getZ());
                player.teleportTo(safe.x, safe.y, safe.z);
                ctx.getSource().sendSuccess(() ->
                    Component.literal("§aJoined team §e" + team + "§a and teleported to spawn"), true);
                return Command.SINGLE_SUCCESS;
            }
        }

        ctx.getSource().sendSuccess(() ->
            Component.literal("§aJoined team §e" + team), true);
        return Command.SINGLE_SUCCESS;
    }

    private int leaveTeam(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("§cMust be used by a player"));
            return 0;
        }

        if (!teamStorage.hasTeam(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal("§cYou are not on a team"));
            return 0;
        }

        teamStorage.removePlayer(player.getUUID());
        ctx.getSource().sendSuccess(() ->
            Component.literal("§aYou left your team"), true);
        return Command.SINGLE_SUCCESS;
    }
}