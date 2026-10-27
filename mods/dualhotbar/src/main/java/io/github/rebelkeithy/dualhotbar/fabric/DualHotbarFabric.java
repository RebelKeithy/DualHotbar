package io.github.rebelkeithy.dualhotbar.fabric;

import io.github.rebelkeithy.dualhotbar.DualHotbar;
import io.github.rebelkeithy.dualhotbar.DualHotbarKeys;
import io.github.rebelkeithy.dualhotbar.HotbarRenderer;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

// Fabric client entry point, listed under "client" in fabric.mod.json. The mod is client-only. The settings screen is
// reached through Mod Menu when it's installed (DualHotbarModMenu) or the "Open Dual Hotbar Settings" key.
public class DualHotbarFabric implements ClientModInitializer {
    // Vanilla HUD elements drawn on top of the hotbar, moved up to make room for the second bar. The info bar's
    // foreground (locator bar dots) is moved by fabric/mixin/InfoBarForegroundMixin.
    private static final List<Identifier> ABOVE_HOTBAR = List.of(
            VanillaHudElements.ARMOR_BAR,
            VanillaHudElements.HEALTH_BAR,
            VanillaHudElements.FOOD_BAR,
            VanillaHudElements.AIR_BAR,
            VanillaHudElements.MOUNT_HEALTH,
            VanillaHudElements.INFO_BAR,
            VanillaHudElements.EXPERIENCE_LEVEL,
            VanillaHudElements.HELD_ITEM_TOOLTIP,
            VanillaHudElements.OVERLAY_MESSAGE,
            VanillaHudElements.CHAT
    );

    @Override
    public void onInitializeClient() {
        DualHotbar.init("fabric");
        DualHotbarKeys.create(KeyMapping.Category.register(DualHotbarKeys.CATEGORY_ID)).forEach(KeyMappingHelper::registerKeyMapping);
        ClientTickEvents.END_CLIENT_TICK.register(DualHotbarKeys::tick);
        // HOTBAR is vanilla's item hotbar only; the spectator menu is a separate element and stays as it is.
        HudElementRegistry.replaceElement(VanillaHudElements.HOTBAR, vanilla -> HotbarRenderer::extractHotbars);
        for (Identifier id : ABOVE_HOTBAR) {
            HudElementRegistry.replaceElement(id, DualHotbarFabric::shiftedUp);
        }
    }

    private static HudElement shiftedUp(HudElement vanilla) {
        return (graphics, deltaTracker) -> HotbarRenderer.extractShiftedUp(
                graphics, HotbarRenderer.decorationShift(), () -> vanilla.extractRenderState(graphics, deltaTracker)
        );
    }
}
