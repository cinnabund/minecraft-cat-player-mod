package com.example.catplayer;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.cat.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Map;
import java.util.UUID;

public final class CatSpawner {
    private CatSpawner() {
    }

    public static void spawnCatForPlayer(PlayerEntity player, ServerWorld world, Map<UUID, CatEntity> playerCats) {
        if (player == null || world == null) {
            return;
        }

        // Don't spawn if one already exists
        if (playerCats.containsKey(player.getUuid())) {
            return;
        }

        // Create a cat entity at the player's position
        CatEntity cat = new CatEntity(EntityType.CAT, world);
        cat.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        cat.setCustomName(player.getName());
        cat.setCustomNameVisible(true);

        // Spawn the cat into the world
        world.spawnEntity(cat);
        playerCats.put(player.getUuid(), cat);
    }

    public static void removeCatForPlayer(PlayerEntity player, Map<UUID, CatEntity> playerCats) {
        if (player == null) {
            return;
        }

        CatEntity cat = playerCats.remove(player.getUuid());
        if (cat != null && !cat.isRemoved()) {
            cat.discard();
        }
    }
}
