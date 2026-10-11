package com.g2806.radiante.mixin.backend;

import com.g2806.radiante.client.RadianteClient;
import com.g2806.radiante.client.compat.OpenGlMods;
import com.g2806.radiante.client.option.Options;
import com.mojang.renderpearl.api.device.GpuBackend;
import com.mojang.renderpearl.backend.opengl.GlBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanBackend;
import net.minecraft.client.PreferredGraphicsApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The renderer shares Minecraft's Vulkan device, and Minecraft's own default prefers OpenGL, so left alone the mod
 * would never run. Vulkan is therefore asked for when the player has expressed no preference - but a player who
 * picked OpenGL on purpose, whether in Minecraft's video settings or because their card cannot ray trace, gets
 * OpenGL and the mod simply stays out of the way. With Iris installed, OpenGL is the default instead (see OpenGlMods).
 */
@Mixin(PreferredGraphicsApi.class)
public class PreferredGraphicsApiMixin {

    @Inject(method = "getBackendsToTry", at = @At("HEAD"), cancellable = true)
    private void radiante$preferVulkan(CallbackInfoReturnable<GpuBackend[]> cir) {
        PreferredGraphicsApi preference = (PreferredGraphicsApi) (Object) this;
        if (OpenGlMods.irisNeedsOpenGl(preference)) {
            // Iris has already applied its OpenGL code for this start (it reads the same setting before the game
            // starts), and that code crashes on Vulkan. OpenGL only, without Vulkan as a fallback for the same
            // reason; Radiante stays off and GraphicsModeScreen explains why and how to switch.
            RadianteClient.LOGGER.info("Iris is installed and Vulkan was not chosen: starting on OpenGL, Radiante "
                + "stays off for this start");
            cir.setReturnValue(new GpuBackend[]{new GlBackend()});
            return;
        }
        if (preference == PreferredGraphicsApi.OPENGL || Options.useOpenGl || Options.openGlAfterFailedStart) {
            // Vulkan stays as the second choice: on Linux under Wayland (NVIDIA especially) OpenGL often has no EGL
            // display at all, and with OpenGL alone the game would not open after a single failed start.
            cir.setReturnValue(new GpuBackend[]{new GlBackend(), new VulkanBackend()});
            return;
        }

        cir.setReturnValue(new GpuBackend[]{new VulkanBackend()});
    }
}
