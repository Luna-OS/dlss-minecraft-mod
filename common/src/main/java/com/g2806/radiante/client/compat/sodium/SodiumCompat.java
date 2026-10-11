package com.g2806.radiante.client.compat.sodium;

/**
 * Sodium replaces Minecraft's terrain renderer. Radiante builds its own terrain from the level, so most of that does
 * not matter to it, but two things do: Sodium takes over the methods that report changed blocks (see
 * SodiumSectionUpdatesMixin) and draws its terrain through a subclass of Minecraft's chunk list (see
 * TerrainDrawSkipMixin). Detected by class, so this works on every loader and costs nothing without Sodium.
 */
public final class SodiumCompat {

    private static final boolean INSTALLED = SodiumCompat.class.getClassLoader()
        .getResource("net/caffeinemc/mods/sodium/client/render/SodiumWorldRenderer.class") != null;

    private SodiumCompat() {
    }

    public static boolean isInstalled() {
        return INSTALLED;
    }
}
