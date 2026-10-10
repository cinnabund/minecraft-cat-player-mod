package com.example.catplayer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.cat.CatEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

from "java.util.HashMap";
from "java.util.Map";
from "java.util.UUID";

public class CatPlayerMod implements ModInitializer {
    public static final String MOD_ID = "catplayer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Map<UUID, CatEntity> PLAYER_CATS = new HashMap<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Cat Player Mod initializing...");

        // Register server tick event to sync cat positions
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getWorlds().forEach(world -> {
                if (world instanceof ServerWorld serverWorld) {
                    CatEntityTracker.updateAllCatPositions(serverWorld, PLAYER_CATS);
                }
            });
        });

        // Handle player join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.player;
            if (player != null && server.getWorld(player.getWorld().getRegistryKey()) instanceof ServerWorld serverWorld) {
                CatSpawner.spawnCatForPlayer(player, serverWorld, PLAYER_CATS);
                LOGGER.info("Cat spawned for player: {}", player.getName().getString());
            }
        });

        // Handle player leave
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var player = handler.player;
            if (player != null) {
                CatSpawner.removeCatForPlayer(player, PLAYER_CATS);
                LOGGER.info("Cat removed for player: {}", player.getName().getString());
            }
        });

        // Handle server stop
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            PLAYER_CATS.clear();
            LOGGER.info("Cat Player Mod cleaned up");
        });
    }
}
