# Cat Companion

Server-side proof-of-concept Fabric mod for Minecraft Java Edition 1.20.1.

This project creates a real vanilla Minecraft cat companion for each player. The cat follows the player during normal survival gameplay. The cat is a separate entity and does not replace the player model.

Important scope rule:
- This project does not replace the player entity or its client renderer.
- The player remains a regular player with normal controls, inventory, health, hitbox, and camera.
- The cat is a separate vanilla cat entity spawned by the server.
- This is intentionally only a server-side companion proof of concept.

## Features
- Spawns one cat per player
- Stores owner UUID on the cat so ownership is known even across reconnects
- Reuses existing valid companions instead of creating duplicates
- Keeps cats following the owner naturally behind them
- Teleports only as a safety fallback when a cat is far away or stuck
- Handles player join, disconnect, respawn, and repeated reconnects
- Works on a dedicated Fabric server without any client mod installation

## Requirements
- Java 17
- Fabric Loader compatible with Minecraft 1.20.1
- Fabric API compatible with Minecraft 1.20.1
- Minecraft Java Edition 1.20.1

## Build

From the project root:

```bash
./gradlew build
```

If Gradle wrapper is not present yet, generate it once:

```bash
gradle wrapper --gradle-version 8.7
./gradlew build
```

## Install on a dedicated Fabric server

1. Build the JAR with Gradle.
2. Copy the output JAR from `build/libs/` to the server's `mods/` directory.
3. Start the Fabric 1.20.1 server.
4. Join the world and each player will receive one cat companion.

## Testing checklist

- Join the server as one player: one cat spawns
- Join with multiple players: each player gets their own cat
- Disconnect and reconnect: no duplicate cats
- Death and respawn: the player still has exactly one companion
- Server restart: existing cat companions restore when world loads, then players are re-associated if valid
- Move away from the cat: it follows naturally
- Move far enough to test fallback: cat can recover to a safe nearby location
- Test on a dedicated server, not only integrated single-player

## Runtime note

This project intentionally does not claim that players appear as cats to vanilla Java or Bedrock clients. The visible player entity remains normal.

The cat is a separate gameplay companion entity meant to be a proof of concept for server-side companion behavior and future client/Bedrock work.

## Project structure

- `src/main/java/com/example/catcompanion/CatCompanionMod.java` - mod entry point
- `src/main/java/com/example/catcompanion/CompanionManager.java` - owner/cat tracking and lifecycle
- `src/main/resources/fabric.mod.json` - mod metadata

## Important limitations

- This is not a custom player model replacement.
- No client-side rendering or camera height changes are included.
- Bedrock/Geyser compatibility is not claimed beyond ordinary vanilla cat entity translation.
- The cat is separate from the player and cannot replace the player's hitbox, self-view, or inventory/rendering.

If you want a true cat-player appearance for all clients, that requires a separate Java client mod and/or a Bedrock-compatible protocol extension. This project intentionally does not do that.
