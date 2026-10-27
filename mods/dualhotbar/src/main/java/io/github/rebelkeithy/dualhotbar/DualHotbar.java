package io.github.rebelkeithy.dualhotbar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Code shared by every loader and Minecraft version. Loader-specific wiring lives in the fabric and neoforge packages.
// The mod is client-only: it swaps items with the same inventory clicks a player could make, so servers don't need it.
public final class DualHotbar {
    public static final String MOD_ID = "dualhotbar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // The second hotbar is the bottom row of the main inventory (inventory indices 27-35), directly above the hotbar
    // in the inventory screen. In the player's inventory menu these slots have the same numbers as their indices.
    public static final int SECOND_BAR_START = 27;

    private DualHotbar() {
    }

    // The open screen, or null in game. Screens moved from Minecraft to Gui in 26.2.
    public static @Nullable Screen screen() {
        //? if >=26.2 {
        return Minecraft.getInstance().gui.screen();
        //?} else {
        /*return Minecraft.getInstance().screen;
        *///?}
    }

    // Opens a screen, or returns to the game for null.
    public static void setScreen(@Nullable Screen screen) {
        //? if >=26.2 {
        Minecraft.getInstance().gui.setScreen(screen);
        //?} else {
        /*Minecraft.getInstance().setScreen(screen);
        *///?}
    }

    // Called once by each loader's client entry point.
    public static void init(String loader) {
        LOGGER.info("Dual Hotbar loaded on {}", loader);
    }
}
