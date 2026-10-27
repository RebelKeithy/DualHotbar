package io.github.rebelkeithy.dualhotbar.neoforge;

import io.github.rebelkeithy.dualhotbar.DualHotbar;
import io.github.rebelkeithy.dualhotbar.DualHotbarKeys;
import io.github.rebelkeithy.dualhotbar.DualHotbarSettingsScreen;
import io.github.rebelkeithy.dualhotbar.HotbarRenderer;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

// NeoForge entry point. The mod is client-only, so it only has a client @Mod class. The settings screen is the config
// button in the Mods list, or the "Open Dual Hotbar Settings" key.
@Mod(value = DualHotbar.MOD_ID, dist = Dist.CLIENT)
public class DualHotbarNeoForge {
    // NeoForge stacks the status bars (health, armor, food, air, mount health) and modded bars on leftHeight and
    // rightHeight, which the hotbar layer raises below. These layers have fixed positions and are moved up instead.
    private static final List<Identifier> FIXED_ABOVE_HOTBAR = List.of(
            VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND,
            VanillaGuiLayers.EXPERIENCE_LEVEL,
            VanillaGuiLayers.CONTEXTUAL_INFO_BAR,
            VanillaGuiLayers.CHAT
    );
    // The held item name and overlay message sit above max(leftHeight, rightHeight), but never lower than vanilla's
    // 59 pixels (+ 9 for the overlay message), which assumes a single hotbar.
    private static final int VANILLA_TEXT_FLOOR = 59;

    public DualHotbarNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        DualHotbar.init("neoforge");
        modEventBus.addListener(DualHotbarNeoForge::registerGuiLayers);
        modEventBus.addListener(DualHotbarNeoForge::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> DualHotbarKeys.tick(Minecraft.getInstance()));
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, modListScreen) -> new DualHotbarSettingsScreen(modListScreen));
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        KeyMapping.Category category = new KeyMapping.Category(DualHotbarKeys.CATEGORY_ID);
        event.registerCategory(category);
        DualHotbarKeys.create(category).forEach(event::register);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.wrapLayer(VanillaGuiLayers.HOTBAR, vanilla -> (graphics, deltaTracker) -> {
            if (Minecraft.getInstance().gameMode.getPlayerMode() == GameType.SPECTATOR) {
                vanilla.render(graphics, deltaTracker);
                return;
            }
            HotbarRenderer.extractHotbars(graphics, deltaTracker);
            int shift = HotbarRenderer.decorationShift();
            hud().leftHeight += shift;
            hud().rightHeight += shift;
        });
        for (Identifier id : FIXED_ABOVE_HOTBAR) {
            event.wrapLayer(id, layer -> shiftedUp(layer, false));
        }
        event.wrapLayer(VanillaGuiLayers.SELECTED_ITEM_NAME, layer -> shiftedUp(layer, true));
        event.wrapLayer(VanillaGuiLayers.OVERLAY_MESSAGE, layer -> shiftedUp(layer, true));
    }

    private static GuiLayer shiftedUp(GuiLayer layer, boolean raiseTextFloor) {
        return (graphics, deltaTracker) -> {
            int shift = HotbarRenderer.decorationShift();
            if (raiseTextFloor) {
                // Only the part of the shift that leftHeight and rightHeight don't already cover.
                int stacked = Math.max(hud().leftHeight, hud().rightHeight);
                shift = Math.max(stacked, VANILLA_TEXT_FLOOR + shift) - Math.max(stacked, VANILLA_TEXT_FLOOR);
            }
            HotbarRenderer.extractShiftedUp(graphics, shift, () -> layer.render(graphics, deltaTracker));
        };
    }

    //? if >=26.2 {
    private static Hud hud() {
        return Minecraft.getInstance().gui.hud;
    }
    //?} else {
    /*private static Gui hud() {
        return Minecraft.getInstance().gui;
    }
    *///?}
}
