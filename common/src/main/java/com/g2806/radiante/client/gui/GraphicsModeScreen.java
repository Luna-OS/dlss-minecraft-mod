package com.g2806.radiante.client.gui;

import com.g2806.radiante.client.compat.OpenGlMods;
import com.g2806.radiante.client.option.Options;
import java.util.List;
import net.minecraft.client.PreferredGraphicsApi;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * With Iris or Nvidium installed next to Radiante: says which of them runs in this start and offers to switch. They
 * cannot run together - Radiante needs Minecraft's Vulkan backend, Iris and Nvidium need OpenGL, and the game picks
 * one when it starts - so the switch sets the graphics API and asks for a restart. Shown once on the first menu after
 * startup unless the player turned it off, and any time from the Radiante key while Radiante is not running.
 */
public class GraphicsModeScreen extends Screen {

    private static final int WIDGET_WIDTH = 240;

    private static boolean shown;

    private final Screen parent;
    private final boolean onVulkan;

    public GraphicsModeScreen(Screen parent) {
        super(Component.translatable(OpenGlMods.runningOnVulkan() ? "screen.radiante.graphics_mode.title.vulkan"
            : "screen.radiante.graphics_mode.title.opengl"));
        this.parent = parent;
        this.onVulkan = OpenGlMods.runningOnVulkan();
    }

    /** True once per start, and only when there is something to say. */
    public static boolean shouldShow() {
        if (shown || Options.hideGraphicsModeNotice || !OpenGlMods.anyInstalled()) {
            return false;
        }
        shown = true;
        return true;
    }

    @Override
    protected void init() {
        LinearLayout layout = LinearLayout.vertical().spacing(8);
        layout.defaultCellSetting().alignHorizontallyCenter();

        String names = OpenGlMods.names();
        layout.addChild(new MultiLineTextWidget(this.title, this.font).setMaxWidth(320).setCentered(true));
        layout.addChild(new MultiLineTextWidget(Component.translatable(this.onVulkan
            ? "screen.radiante.graphics_mode.message.vulkan" : "screen.radiante.graphics_mode.message.opengl", names),
            this.font).setMaxWidth(320).setCentered(true));

        layout.addChild(Button.builder(Component.translatable(this.onVulkan
            ? "screen.radiante.graphics_mode.switch.opengl" : "screen.radiante.graphics_mode.switch.vulkan", names),
            button -> this.switchApi()).width(WIDGET_WIDTH).build());
        layout.addChild(Button.builder(Component.translatable("screen.radiante.graphics_mode.continue"),
            button -> this.onClose()).width(WIDGET_WIDTH).build());
        if (!Options.hideGraphicsModeNotice) {
            layout.addChild(Button.builder(Component.translatable("screen.radiante.graphics_mode.hide"), button -> {
                Options.hideGraphicsModeNotice = true;
                Options.overwriteConfig();
                this.onClose();
            }).width(WIDGET_WIDTH).build());
        }

        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
        layout.setPosition(this.width / 2 - layout.getWidth() / 2, this.height / 2 - layout.getHeight() / 2);
    }

    /**
     * Vulkan for Radiante, OpenGL for Iris and Nvidium. Both are saved in options.txt, which is also what Iris reads
     * before the next start to decide whether to apply its OpenGL code (see OpenGlMods).
     */
    private void switchApi() {
        boolean toVulkan = !this.onVulkan;
        this.minecraft.options.preferredGraphicsBackend().set(toVulkan ? PreferredGraphicsApi.VULKAN
            : PreferredGraphicsApi.OPENGL);
        this.minecraft.options.save();
        if (toVulkan && Options.useOpenGl) {
            // Radiante's own "run on OpenGL" choice would otherwise keep the game on OpenGL.
            Options.useOpenGl = false;
            Options.overwriteConfig();
        }
        this.minecraft.gui.setScreen(new RestartRequiredScreen(this.parent, List.of(Component.translatable(toVulkan
            ? "screen.radiante.graphics_mode.change.vulkan" : "screen.radiante.graphics_mode.change.opengl",
            OpenGlMods.names()))));
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
