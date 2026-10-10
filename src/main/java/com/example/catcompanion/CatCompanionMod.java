package com.example.catcompanion;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatCompanionMod implements ModInitializer {
    public static final String MOD_ID = "catcompanion";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("CatCompanion initializing");

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CompanionManager.rebuildAllCompanions(server);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            CompanionManager.ensureCompanion(handler.player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            CompanionManager.removeCompanion(handler.player);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            CompanionManager.tick(server);
        });
    }
}
