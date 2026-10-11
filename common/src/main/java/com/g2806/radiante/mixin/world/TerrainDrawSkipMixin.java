package com.g2806.radiante.mixin.world;

import com.g2806.radiante.client.render.RadianteRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * No terrain of Minecraft's over the traced picture, whoever draws it. ChunkSectionsToRenderMixin stops Minecraft's
 * own terrain draws, but a mod that replaces the terrain renderer hands back a subclass that overrides them: Sodium's
 * SodiumChunkSection drew its whole terrain over the traced world. Skipping the calls where the level render makes
 * them catches any subclass.
 *
 * <p>The low priority applies this before other mods' mixins, which puts these wrappers innermost: Fabric API wraps
 * the same calls to fire its level render events (start of the main pass, after opaque terrain, around translucent
 * terrain), and those still fire for the mods that listen to them; only the terrain draw itself is left out.
 */
@Mixin(value = LevelRenderer.class, priority = 500)
public class TerrainDrawSkipMixin {

    private static final String RENDER_GROUP = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup("
        + "Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/renderpearl/api/commands/RenderPass;"
        + "Lcom/mojang/renderpearl/api/textures/GpuSampler;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Z)V";

    @WrapOperation(method = {"executeSolid", "executeClassicTransparency"},
        at = @At(value = "INVOKE", target = RENDER_GROUP), require = 2)
    private void radiante$noTerrainGroupOverTrace(ChunkSectionsToRender sections,
        net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup group,
        com.mojang.renderpearl.api.commands.RenderPass renderPass, com.mojang.renderpearl.api.textures.GpuSampler sampler,
        com.mojang.renderpearl.api.textures.GpuTextureView atlas, boolean wireframe, Operation<Void> original) {
        if (!RadianteRenderer.isOverlayFrame()) {
            original.call(sections, group, renderPass, sampler, atlas, wireframe);
        }
    }

    @WrapOperation(method = "executeOit", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderOit(Lcom/mojang/renderpearl/api/textures/GpuSampler;Lnet/minecraft/client/renderer/oit/OitStage;Lnet/minecraft/client/renderer/oit/OitRenderPassProvider$Parameters;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lcom/mojang/renderpearl/api/textures/GpuTextureView;)V"))
    private void radiante$noOitTerrainOverTrace(ChunkSectionsToRender sections,
        com.mojang.renderpearl.api.textures.GpuSampler sampler, net.minecraft.client.renderer.oit.OitStage stage,
        net.minecraft.client.renderer.oit.OitRenderPassProvider.Parameters params,
        com.mojang.renderpearl.api.textures.GpuTextureView atlas,
        com.mojang.renderpearl.api.textures.GpuTextureView lightmap, Operation<Void> original) {
        if (!RadianteRenderer.isOverlayFrame()) {
            original.call(sections, sampler, stage, params, atlas, lightmap);
        }
    }
}
