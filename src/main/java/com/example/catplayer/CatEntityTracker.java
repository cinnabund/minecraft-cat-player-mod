package com.example.catplayer;

import net.minecraft.entity.cat.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Map;
import java.util.UUID;

public final class CatEntityTracker {
    private CatEntityTracker() {
    }

    public static void updateAllCatPositions(ServerWorld world, Map<UUID, CatEntity> playerCats) {
        if (world == null) {
            return;
        }

        for (PlayerEntity player : world.getPlayers(p -> true)) {
            CatEntity cat = playerCats.get(player.getUuid());
            if (cat != null && !cat.isRemoved()) {
                // Sync cat position and rotation to player
                cat.setPos(player.getX(), player.getY(), player.getZ());
                cat.setYaw(player.getYaw());
                cat.setPitch(player.getPitch());
                cat.setHeadYaw(player.getHeadYaw());

                // Mark the cat as changed so clients receive update
                cat.velocityDirty = true;
            }
        }
    }
}
