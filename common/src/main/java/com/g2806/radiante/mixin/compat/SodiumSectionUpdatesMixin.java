package com.g2806.radiante.mixin.compat;

import com.g2806.radiante.client.compat.sodium.SodiumCompat;
import net.minecraft.client.SectionUpdateTracker;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Radiante learns which sections changed from Minecraft's section update tracker (SectionDirtyStateMixin). Sodium
 * overwrites the four methods that mark sections dirty so they only feed its own renderer, and the tracker never
 * hears of a changed block: with Sodium installed, placed and broken blocks stayed as they were in the traced world.
 * These callbacks sit at the head of Sodium's replacements (injectors are applied after overwrites) and mark the
 * tracker the way Minecraft's own methods would. Without Sodium they do nothing, since Minecraft's methods already do.
 */
@Mixin(LevelExtractor.class)
public class SodiumSectionUpdatesMixin {

    @Shadow
    private @Nullable SectionUpdateTracker sectionUpdateTracker;

    @Inject(method = "setSectionDirty(IIIZ)V", at = @At("HEAD"))
    private void radiante$sodiumSectionDirty(int sectionX, int sectionY, int sectionZ, boolean playerChanged,
        CallbackInfo ci) {
        if (SodiumCompat.isInstalled()) {
            this.radiante$markDirty(sectionX, sectionY, sectionZ, playerChanged);
        }
    }

    @Inject(method = "setBlockDirty(Lnet/minecraft/core/BlockPos;Z)V", at = @At("HEAD"))
    private void radiante$sodiumBlockDirty(BlockPos pos, boolean playerChanged, CallbackInfo ci) {
        if (SodiumCompat.isInstalled()) {
            this.radiante$markBlocks(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ(),
                playerChanged);
        }
    }

    @Inject(method = "setBlocksDirty", at = @At("HEAD"))
    private void radiante$sodiumBlocksDirty(int x0, int y0, int z0, int x1, int y1, int z1, CallbackInfo ci) {
        if (SodiumCompat.isInstalled()) {
            this.radiante$markBlocks(x0, y0, z0, x1, y1, z1, false);
        }
    }

    @Inject(method = "setSectionDirtyWithNeighbors", at = @At("HEAD"))
    private void radiante$sodiumSectionDirtyWithNeighbors(int sectionX, int sectionY, int sectionZ, CallbackInfo ci) {
        if (SodiumCompat.isInstalled()) {
            for (int z = sectionZ - 1; z <= sectionZ + 1; z++) {
                for (int x = sectionX - 1; x <= sectionX + 1; x++) {
                    for (int y = sectionY - 1; y <= sectionY + 1; y++) {
                        this.radiante$markDirty(x, y, z, false);
                    }
                }
            }
        }
    }

    /** Every section touching the blocks one past the given box, as Minecraft marks them. */
    @Unique
    private void radiante$markBlocks(int x0, int y0, int z0, int x1, int y1, int z1, boolean playerChanged) {
        int minX = SectionPos.blockToSectionCoord(x0 - 1);
        int minY = SectionPos.blockToSectionCoord(y0 - 1);
        int minZ = SectionPos.blockToSectionCoord(z0 - 1);
        int maxX = SectionPos.blockToSectionCoord(x1 + 1);
        int maxY = SectionPos.blockToSectionCoord(y1 + 1);
        int maxZ = SectionPos.blockToSectionCoord(z1 + 1);
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    this.radiante$markDirty(x, y, z, playerChanged);
                }
            }
        }
    }

    @Unique
    private void radiante$markDirty(int sectionX, int sectionY, int sectionZ, boolean playerChanged) {
        SectionUpdateTracker tracker = this.sectionUpdateTracker;
        if (tracker != null) {
            tracker.setDirty(sectionX, sectionY, sectionZ, playerChanged);
        }
    }
}
