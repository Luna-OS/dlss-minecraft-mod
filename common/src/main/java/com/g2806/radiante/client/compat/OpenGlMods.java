package com.g2806.radiante.client.compat;

import com.g2806.radiante.platform.RadiantePlatform;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.PreferredGraphicsApi;

/**
 * Mods that only work on Minecraft's OpenGL backend: Iris (shader packs) and Nvidium. Radiante only works on the
 * Vulkan backend, and the game picks one of the two when it starts, so they can be installed together but never run
 * together. Which one runs is the player's choice of graphics API, switched with a restart (GraphicsModeScreen).
 *
 * <p>Iris decides before the game starts which of its mixins to apply: it reads {@code preferredGraphicsBackend}
 * from options.txt and applies its OpenGL code unless that says Vulkan (IrisMixinPlugin). Starting on Vulkan after
 * Iris has applied its OpenGL code crashes, so the backend choice here follows exactly the same rule.
 */
public final class OpenGlMods {

    private static Boolean iris;
    private static Boolean nvidium;
    private static volatile boolean vulkanDeviceCreated;

    private OpenGlMods() {
    }

    public static boolean irisInstalled() {
        if (iris == null) {
            iris = RadiantePlatform.INSTANCE.isModLoaded("iris");
        }
        return iris;
    }

    public static boolean nvidiumInstalled() {
        if (nvidium == null) {
            nvidium = RadiantePlatform.INSTANCE.isModLoaded("nvidium");
        }
        return nvidium;
    }

    public static boolean anyInstalled() {
        return irisInstalled() || nvidiumInstalled();
    }

    /** "Iris", "Nvidium" or "Iris, Nvidium", for messages. */
    public static String names() {
        List<String> names = new ArrayList<>();
        if (irisInstalled()) {
            names.add("Iris");
        }
        if (nvidiumInstalled()) {
            names.add("Nvidium");
        }
        return String.join(", ", names);
    }

    /**
     * True when Iris is installed and has applied its OpenGL code for this start: everything but an explicit Vulkan
     * in options.txt. The game must then start on OpenGL, and only on OpenGL.
     */
    public static boolean irisNeedsOpenGl(PreferredGraphicsApi preference) {
        return irisInstalled() && preference != PreferredGraphicsApi.VULKAN;
    }

    /** Called when Minecraft's Vulkan device exists, which is how this start's graphics API is known. */
    public static void onVulkanDeviceCreated() {
        vulkanDeviceCreated = true;
    }

    /** Whether this start runs on Vulkan (Radiante's side) rather than OpenGL (Iris' and Nvidium's). */
    public static boolean runningOnVulkan() {
        return vulkanDeviceCreated;
    }
}
