# Cat Player Mod for Fabric 1.20.1

**Server-side proof-of-concept that spawns a cat entity alongside each player, while keeping the real player entity intact for survival gameplay.**

## ⚠️ CRITICAL LIMITATIONS

### What DOES NOT Work
- **Unmodded Java clients will NOT see players as cats.** Vanilla Minecraft clients always render player entities as humanoids with their skin. There is no standard network protocol for "disguise the player as a different model."
- **Bedrock clients connecting via Geyser will also NOT see cats.** Geyser translates vanilla entities, but a custom disguise system is not part of vanilla Minecraft or Geyser's supported protocol.
- **First-person camera height:** The local player's camera remains at normal player height. The cat entity is a separate visual, not the player's actual model.
- **Collisions:** The player's hitbox remains normal (1.8m tall). The cat is cosmetic and does not affect physics or survival gameplay.
- **Item rendering:** The player sees items in their hands from the normal player model perspective, not from a cat's perspective.

### What DOES Work (Server-Side Only)
- The server spawns a cat entity at each player's position.
- The cat's position and head rotation follow the player's movement in real-time.
- Survival gameplay (inventory, health, mining, crafting, combat) is completely unaffected because the player entity remains a normal player.
- The cat despawns when the player leaves and respawns on join/respawn.
- **This implementation is compatible with vanilla servers IF deployed as a server-side only mod.**

## Why This Limitation Exists

**Minecraft's network protocol does not support "model replacement" for player entities.** The protocol transmits:
- Player spawn packet (UUID, skin, name)
- Entity metadata (pose, equipment, custom name tag)
- Entity movement (position, rotation, velocity)

None of these packets carry information about disguising as a different entity type. To achieve a "cat player" visible on vanilla clients, you would need:

1. Intercept the player spawn packet and modify it to be a cat entity spawn packet (requires client mod).
2. Hide the real player entity from all clients (requires custom packet handling, breaks survival mechanics).
3. Sync all player state (health, equipment, name tag, death) to the fake cat entity (complex and fragile).

**This is fundamentally incompatible with:**
- Vanilla (unmodded) client expectations.
- Geyser's entity translation system (Bedrock cannot use protocol data it doesn't understand).
- Normal survival gameplay if the player entity is hidden.

## What This Mod Actually Does

This is a **proof-of-concept server-side implementation:**
- Spawns a cat entity that follows each player in real-time.
- The cat is visible only to other players and servers that render the cat entity correctly.
- The player remains a normal survival entity with full gameplay access.
- If a player is in creative mode, the cat is hidden (cosmetic only, no conflict).
- Cats despawn on player disconnect and respawn on reconnect or after respawn.

**This works on paper for vanilla servers but does NOT satisfy the requirement that unmodded clients see players as cats.**

## Architecture

### Server-Side Components
- **CatPlayerMod.java** - Main mod entry point.
- **CatEntityManager.java** - Tracks active players and manages cat entity spawning/despawning.
- **CatEntityTracker.java** - Syncs cat position and rotation with player movement.
- **CatSpawner.java** - Handles cat entity creation and resource management.

### Limitations Summary Table

| Feature | Works? | Notes |
|---------|--------|-------|
| Vanilla clients see cat | ❌ | Not possible without client mod |
| Geyser/Bedrock support | ❌ | Bedrock clients will see normal player or error |
| Server-side only | ✅ | No client mod required on players' machines |
| Survival gameplay preserved | ✅ | Player is normal entity, cat is cosmetic |
| Player movement syncs | ✅ | Cat follows player in real-time |
| Camera height adjustment | ❌ | Player camera remains at normal height |
| Item/armor rendering | ❌ | Items render on player model, not cat |
| Collision/physics | ❌ | Player hitbox unchanged (1.8m tall) |

## Requirements

- Java 17 or higher
- Fabric Loader 0.15.11+
- Minecraft 1.20.1
- Fabric API 0.92.2+1.20.1

## Building

```bash
./gradlew clean build
```

The compiled mod JAR will be at `build/libs/catplayer-*.jar`.

## Installation

1. Install Fabric for Minecraft 1.20.1 on your server.
2. Copy the built JAR to the server's `mods/` folder.
3. Restart the server.
4. On join or respawn, each player will have a cat entity spawned alongside them.

**Note:** This is a server-side mod. Clients connecting to the server do NOT need the mod installed. However, they will only see cats if:
- They have a mod that allows server-side entity rendering customization.
- The server also sends appropriate client-side texture/model data (not included in this implementation).

## Testing

1. Start a local Fabric 1.20.1 server with this mod.
2. Connect with a vanilla client.
3. Observe: You will see normal players, not cats. Other players will also see you as a normal player.
4. If you connect with another server running this mod (with all players having the mod), you may see cat entities at player positions on the server's debug output, but vanilla clients still won't render them correctly.

## Geyser Compatibility

**This mod is NOT compatible with Geyser in the sense that Bedrock clients will NOT see players as cats.** Here's why:

- Geyser translates Minecraft Java Edition entities to Bedrock Edition using the Bedrock protocol.
- The Bedrock protocol has a fixed set of entity types (cow, cat, player, etc.).
- A cat entity spawned via the Fabric mod is a normal Minecraft cat.
- However, Geyser expects the "player" entity to represent the player in the Bedrock world.
- Spawning a separate cat entity causes confusion: Bedrock sees two entities (the player and a cat) but doesn't know they're linked.
- Bedrock clients cannot equip items on a cat or interact with it as a player.

For true Bedrock player appearance as a cat:
- You would need a custom Geyser extension that explicitly handles cat-player mapping.
- Or, you would need a client-side Bedrock mod (not available for vanilla Bedrock).

## Future Work

If you want full cross-platform cat appearance:
1. **Client-side Fabric mod for Java:** Apply cat model/texture to the local player renderer.
2. **Custom Geyser extension:** Add protocol translation for cat-player mapping.
3. **Client-side Bedrock mod (if available):** Apply cat model/texture on Bedrock clients.

These are separate projects and fall outside the scope of a "server-only" implementation.

## Summary

**This mod proves that a server-side entity spawn system can work, but it does NOT achieve the goal of making unmodded players see cats.** The Minecraft network protocol and Geyser's design do not support transparent model replacement for players.

If your actual goal is to make players appear as cats to everyone, you need:
1. A client-side mod on all Java clients.
2. A custom Geyser extension for Bedrock support.
3. Acceptance that first-person perspective, collisions, and item rendering will remain player-like.

This implementation is provided as a proof-of-concept to demonstrate why those requirements are necessary and what a pure server-side approach can and cannot achieve.
