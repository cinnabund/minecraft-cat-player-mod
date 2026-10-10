package com.example.catcompanion;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class CompanionManager {
    private static final Map<UUID, CatEntity> COMPANIONS = new HashMap<>();

    private CompanionManager() {
    }

    public static void rebuildAllCompanions(net.minecraft.server.MinecraftServer server) {
        COMPANIONS.clear();

        for (ServerWorld world : server.getWorlds()) {
            for (CatEntity cat : world.getEntitiesByClass(CatEntity.class, entity -> entity != null)) {
                UUID ownerUuid = cat.getOwnerUuid();
                if (ownerUuid != null) {
                    PlayerEntity owner = world.getPlayerByUuid(ownerUuid);
                    if (owner != null) {
                        COMPANIONS.put(ownerUuid, cat);
                    }
                }
            }
        }
    }

    public static void ensureCompanion(PlayerEntity player) {
        if (player == null || player.getWorld().isClient) {
            return;
        }

        ServerWorld world = (ServerWorld) player.getWorld();
        UUID ownerId = player.getUuid();
        CatEntity existing = COMPANIONS.get(ownerId);
        if (existing != null && !existing.isRemoved()) {
            if (existing.getOwnerUuid() == null || !ownerId.equals(existing.getOwnerUuid())) {
                existing.setOwnerUuid(ownerId);
            }
            existing.setOwner(player);
            return;
        }

        CatEntity cat = findExistingCompanion(world, ownerId);
        if (cat == null) {
            cat = createCompanion(player, world);
        }

        if (cat != null) {
            cat.setOwnerUuid(ownerId);
            cat.setOwner(player);
            COMPANIONS.put(ownerId, cat);
        }
    }

    public static void removeCompanion(PlayerEntity player) {
        if (player == null) {
            return;
        }

        CatEntity cat = COMPANIONS.remove(player.getUuid());
        if (cat != null && !cat.isRemoved()) {
            cat.discard();
        }
    }

    public static void tick(net.minecraft.server.MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            for (PlayerEntity player : world.getPlayers()) {
                ensureCompanion(player);
                updateFollowBehavior(player);
            }
        }
    }

    private static void updateFollowBehavior(PlayerEntity player) {
        if (player == null || player.getWorld().isClient) {
            return;
        }

        CatEntity cat = COMPANIONS.get(player.getUuid());
        if (cat == null || cat.isRemoved()) {
            return;
        }

        cat.setOwner(player);
        cat.setOwnerUuid(player.getUuid());
        cat.setPersistent();
        cat.setInSittingPose(false);

        double dx = player.getX() - cat.getX();
        double dz = player.getZ() - cat.getZ();
        double distance = Math.hypot(dx, dz);

        if (distance > 32.0D) {
            BlockPos safePos = findSafeNearbyPosition(player);
            if (safePos != null) {
                cat.teleport(safePos.getX() + 0.5D, safePos.getY(), safePos.getZ() + 0.5D);
            }
            return;
        }

        double angle = Math.atan2(-dx, -dz);
        double behindX = player.getX() + Math.sin(angle) * 1.8D;
        double behindZ = player.getZ() + Math.cos(angle) * 1.8D;
        double targetY = player.getY();

        cat.getNavigation().startMovingTo(behindX, targetY, behindZ, 1.1D);

        if (distance > 8.0D) {
            cat.getNavigation().startMovingTo(behindX, targetY, behindZ, 1.35D);
        }
    }

    private static BlockPos findSafeNearbyPosition(PlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos base = player.getBlockPos();

        for (int offset = 0; offset < 12; offset++) {
            for (int x = -offset; x <= offset; x++) {
                for (int z = -offset; z <= offset; z++) {
                    BlockPos pos = base.add(x, 0, z);
                    BlockPos ground = world.getTopPosition(net.minecraft.entity.EntityPose.STANDING, pos);
                    if (ground != null && world.isAir(ground.down()) == false) {
                        return ground.up();
                    }
                }
            }
        }

        return base;
    }

    private static CatEntity findExistingCompanion(ServerWorld world, UUID ownerId) {
        for (CatEntity cat : world.getEntitiesByClass(CatEntity.class, entity -> entity != null)) {
            if (ownerId.equals(cat.getOwnerUuid())) {
                return cat;
            }
        }
        return null;
    }

    private static CatEntity createCompanion(PlayerEntity player, ServerWorld world) {
        CatEntity cat = EntityType.CAT.create(world);
        if (cat == null) {
            return null;
        }

        cat.refreshPositionAndAngles(
            player.getX(),
            player.getY(),
            player.getZ(),
            player.getYaw(),
            player.getPitch()
        );
        cat.setOwnerUuid(player.getUuid());
        cat.setOwner(player);
        cat.setPersistent();
        cat.setTamed(true);
        cat.setInSittingPose(false);
        cat.setCustomNameVisible(false);

        world.spawnEntity(cat);
        return cat;
    }
}
