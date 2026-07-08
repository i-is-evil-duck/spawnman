package com.j3ly.spawnman.command;

import com.j3ly.spawnman.event.PlayerSpawnHandler;
import com.j3ly.spawnman.model.SpawnArea;
import com.j3ly.spawnman.model.SpawnPoint;
import com.j3ly.spawnman.model.SpawnSet;
import com.j3ly.spawnman.storage.SpawnStorage;
import com.j3ly.spawnman.storage.TeamStorage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public class SpawnManCommand {
    private static final int MAX_MARKERS = 10;
    public static final Map<UUID, Vec3[]> playerMarkers = new HashMap<>();
    private final SpawnStorage storage;
    private TeamStorage teamStorage;

    public SpawnManCommand(SpawnStorage storage, TeamStorage teamStorage) {
        this.storage = storage;
        this.teamStorage = teamStorage;
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("sm")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("set")
                    .then(Commands.literal("point")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .then(Commands.argument("markers", StringArgumentType.greedyString())
                                .executes(this::setPointSpawn))))
                    .then(Commands.literal("area")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx -> setAreaSpawn(ctx, null))
                            .then(Commands.argument("team", StringArgumentType.word())
                                .executes(ctx -> setAreaSpawn(ctx, StringArgumentType.getString(ctx, "team")))))))

                .then(Commands.literal("team")
                    .then(Commands.argument("id", StringArgumentType.word())
                        .then(Commands.argument("team", StringArgumentType.word())
                            .executes(this::setTeam))))

                .then(Commands.literal("remove")
                    .then(Commands.argument("id", StringArgumentType.word())
                        .executes(this::removeSpawnSet)))

                .then(Commands.literal("list")
                    .executes(this::listSpawnSets))

                .then(Commands.literal("tp")
                    .then(Commands.argument("team", StringArgumentType.word())
                        .executes(this::teleportTeam)))
        );

        for (int i = 1; i <= MAX_MARKERS; i++) {
            final int markerNum = i;
            dispatcher.register(
                Commands.literal("smpos" + i)
                    .requires(source -> source.hasPermission(2))
                    .executes(ctx -> setMarker(ctx, markerNum))
            );
        }
    }

    private int setMarker(CommandContext<CommandSourceStack> ctx, int markerNum) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("§cMust be used by a player"));
            return 0;
        }

        Vec3 pos = player.position();
        UUID uuid = player.getUUID();
        playerMarkers.computeIfAbsent(uuid, k -> new Vec3[MAX_MARKERS + 1]);
        playerMarkers.get(uuid)[markerNum] = pos;

        ctx.getSource().sendSuccess(() ->
            Component.literal(String.format("§aPosition %d set to: §e%.1f, %.1f, %.1f",
                markerNum, pos.x, pos.y, pos.z)), false);
        return Command.SINGLE_SUCCESS;
    }

    private int setPointSpawn(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("§cMust be used by a player"));
            return 0;
        }

        String id = StringArgumentType.getString(ctx, "id");
        String markersStr = StringArgumentType.getString(ctx, "markers");

        if (storage.hasSpawnSet(id)) {
            ctx.getSource().sendFailure(Component.literal("§cSpawn set '" + id + "' already exists"));
            return 0;
        }

        SpawnSet set = new SpawnSet(id);
        set.setWorld(player.level().dimension().location().toString());

        String[] parts = markersStr.split("\\s+");
        String last = parts[parts.length - 1];
        String team = null;
        if (!last.replaceAll("[^0-9]", "").matches("\\d+")) {
            team = last;
            markersStr = markersStr.substring(0, markersStr.lastIndexOf(last)).trim();
        }

        String[] markerIds = markersStr.isEmpty() ? new String[0] : markersStr.split("\\s+");
        UUID uuid = player.getUUID();
        Vec3[] markers = playerMarkers.get(uuid);

        if (markers == null) {
            ctx.getSource().sendFailure(Component.literal("§cNo position markers set. Use /smpos1, /smpos2, etc."));
            return 0;
        }

        for (String mId : markerIds) {
            try {
                int num = Integer.parseInt(mId.replaceAll("[^0-9]", ""));
                if (num < 1 || num > MAX_MARKERS || markers[num] == null) {
                    ctx.getSource().sendFailure(Component.literal("§cPosition " + num + " is not set"));
                    return 0;
                }
                Vec3 v = markers[num];
                set.getPoints().add(new SpawnPoint(v.x, v.y, v.z));
            } catch (NumberFormatException e) {
                ctx.getSource().sendFailure(Component.literal("§cInvalid marker: " + mId));
                return 0;
            }
        }

        if (set.getPoints().isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("§cNo valid position markers provided"));
            return 0;
        }

        if (team != null) {
            set.setTeam(team);
        }

        storage.addSpawnSet(set);
        String teamMsg = team != null ? " for team §e" + team : "";
        ctx.getSource().sendSuccess(() ->
            Component.literal("§aSpawn set '§e" + id + "§a' created with " + set.getPoints().size() + " points" + teamMsg), true);
        return Command.SINGLE_SUCCESS;
    }

    private int setAreaSpawn(CommandContext<CommandSourceStack> ctx, String team) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("§cMust be used by a player"));
            return 0;
        }

        String id = StringArgumentType.getString(ctx, "id");

        if (storage.hasSpawnSet(id)) {
            ctx.getSource().sendFailure(Component.literal("§cSpawn set '" + id + "' already exists"));
            return 0;
        }

        UUID uuid = player.getUUID();
        Vec3[] markers = playerMarkers.get(uuid);

        if (markers == null || markers[1] == null || markers[2] == null) {
            ctx.getSource().sendFailure(Component.literal("§cSet /smpos1 and /smpos2 first"));
            return 0;
        }

        SpawnSet set = new SpawnSet(id);
        set.setWorld(player.level().dimension().location().toString());
        set.setArea(new SpawnArea(
            markers[1].x, markers[1].y, markers[1].z,
            markers[2].x, markers[2].y, markers[2].z
        ));
        set.setTeam(team);

        storage.addSpawnSet(set);
        if (team != null) {
            ctx.getSource().sendSuccess(() ->
                Component.literal("§aSpawn area '§e" + id + "§a' created for team §e" + team), true);
        } else {
            ctx.getSource().sendSuccess(() ->
                Component.literal("§aSpawn area '§e" + id + "§a' created"), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int setTeam(CommandContext<CommandSourceStack> ctx) {
        String id = StringArgumentType.getString(ctx, "id");
        String team = StringArgumentType.getString(ctx, "team");

        SpawnSet set = storage.getSpawnSet(id);
        if (set == null) {
            ctx.getSource().sendFailure(Component.literal("§cSpawn set '" + id + "' not found"));
            return 0;
        }

        set.setTeam(team);
        storage.save();
        ctx.getSource().sendSuccess(() ->
            Component.literal("§aSpawn set '§e" + id + "§a' assigned to team §e" + team), true);
        return Command.SINGLE_SUCCESS;
    }

    private int removeSpawnSet(CommandContext<CommandSourceStack> ctx) {
        String id = StringArgumentType.getString(ctx, "id");
        if (!storage.hasSpawnSet(id)) {
            ctx.getSource().sendFailure(Component.literal("§cSpawn set '" + id + "' not found"));
            return 0;
        }
        storage.removeSpawnSet(id);
        ctx.getSource().sendSuccess(() ->
            Component.literal("§aSpawn set '§e" + id + "§a' removed"), true);
        return Command.SINGLE_SUCCESS;
    }

    private int listSpawnSets(CommandContext<CommandSourceStack> ctx) {
        Collection<SpawnSet> allSets = storage.getAllSpawnSets();
        if (allSets.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("§eNo spawn sets defined"), false);
            return Command.SINGLE_SUCCESS;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("§6=== Spawn Sets ==="), false);
        for (SpawnSet set : allSets) {
            String type = set.isArea() ? "Area" : "Points (" + set.getPoints().size() + ")";
            String teamInfo = set.hasTeam() ? "§r | Team: §b" + set.getTeam() : "";
            ctx.getSource().sendSuccess(() -> Component.literal(
                "§e" + set.getId() + "§r | Type: §f" + type + teamInfo + "§r | World: §7" +
                (set.getWorld() != null ? set.getWorld() : "any")), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int teleportTeam(CommandContext<CommandSourceStack> ctx) {
        String team = StringArgumentType.getString(ctx, "team");
        List<ServerPlayer> players = ctx.getSource().getServer().getPlayerList().getPlayers();
        int[] count = {0};
        for (ServerPlayer player : players) {
            if (!team.equals(teamStorage.getPlayerTeam(player.getUUID()))) continue;
            String world = player.level().dimension().location().toString();
            for (SpawnSet set : storage.getSpawnSetsForWorld(world)) {
                if (team.equals(set.getTeam())) {
                    SpawnPoint point = set.getRandomSpawn();
                    if (point != null) {
                        Vec3 safe = PlayerSpawnHandler.findSafeSpawn(player.level(), point.getX(), point.getY(), point.getZ());
                        player.teleportTo(safe.x, safe.y, safe.z);
                        count[0]++;
                    }
                    break;
                }
            }
        }
        int c = count[0];
        ctx.getSource().sendSuccess(() ->
            Component.literal("§aTeleported §e" + c + "§a player(s) to team §e" + team + "§a spawn"), true);
        return Command.SINGLE_SUCCESS;
    }
}