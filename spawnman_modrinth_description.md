# SpawnMan

A lightweight Minecraft Forge 1.20.1 mod for managing custom spawn points and areas with team support.

## What it adds

SpawnMan lets you define named spawn sets as point lists or area boxes per world, assign them to teams, and automatically teleport players to safe spawn locations on join or respawn. Includes a position wand for quick area setup and full command block support.

## Features

- **Spawn Sets** — Define named spawn sets as point lists or area boxes per world
- **Team Spawning** — Assign spawn sets to teams; players on that team spawn at their team's spawn
- **Position Wand** — An item that sets `smpos1` (right-click) and `smpos2` (left-click) for quick area setup
- **Command Block Support** — `/sm tp <team>` teleports all online team members via command blocks
- **Safe Spawning** — Automatically finds solid ground with headroom when teleporting
- **Lightweight** — No UI, no external dependencies, chat-command only

## Commands

All `/sm` commands require operator permission level 2.

| Command | Description |
|---|---|
| `/sm set point <id> <nums>` | Create a point spawn set from marker positions (e.g. `1 2 3`) |
| `/sm set area <id> [team]` | Create an area spawn set from `smpos1` and `smpos2` |
| `/sm team <id> <team>` | Assign a spawn set to a team |
| `/sm remove <id>` | Delete a spawn set |
| `/sm list` | List all spawn sets |
| `/sm tp <team>` | Teleport all online team members to their team's spawn |
| `/smpos1` through `/smpos10` | Save current position as a marker |

### Player Commands

| Command | Description |
|---|---|
| `/team join <team>` | Join a team and teleport to its spawn |
| `/team leave` | Leave your current team |

## Position Wand

```
/give @p spawnman:position_wand
```

- **Right-click a block** → sets `smpos1` at the block's top face
- **Left-click a block** → sets `smpos2` (block is not broken)
- **Left-click an entity** → sets `smpos2` at the entity's position

The wand shares markers with `/smpos1` and `/smpos2` commands.

## How Spawning Works

When a player joins or respawns:

1. If the player is on a team, they teleport to a random point in the team's spawn set (in the current world)
2. Otherwise, they teleport to a random public spawn set (no team) in the current world
3. The spawn point is adjusted to a solid block with 2 blocks of headroom to prevent suffocation

## Installation

1. Install Minecraft Forge 1.20.1
2. Place the `spawnman-1.0.0.jar` in your `mods/` folder
3. Restart the server

## Configuration

Data is stored in `<config-dir>/spawnman/`:

- `spawnsets.json` — all spawn set definitions
- `teams.json` — player-to-team mappings
