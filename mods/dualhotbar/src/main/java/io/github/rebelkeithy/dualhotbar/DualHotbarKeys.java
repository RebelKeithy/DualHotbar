package io.github.rebelkeithy.dualhotbar;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

// Key mappings, listed under "Dual Hotbar" in Controls > Key Binds. Each loader registers the category its own way,
// then registers the mappings create() makes in it.
public final class DualHotbarKeys {
    public static final Identifier CATEGORY_ID = Identifier.fromNamespaceAndPath(DualHotbar.MOD_ID, "general");

    private static KeyMapping swap;
    private static KeyMapping scrollModifier;
    private static KeyMapping openSettings;

    private DualHotbarKeys() {
    }

    public static List<KeyMapping> create(KeyMapping.Category category) {
        swap = new KeyMapping("key.dualhotbar.swap", InputConstants.UNKNOWN.getValue(), category);
        // Shares Left Shift with Sneak by default; both loaders let two mappings use the same key.
        scrollModifier = new KeyMapping("key.dualhotbar.scrollModifier", InputConstants.KEY_LSHIFT, category);
        openSettings = new KeyMapping("key.dualhotbar.settings", InputConstants.UNKNOWN.getValue(), category);
        return List.of(swap, scrollModifier, openSettings);
    }

    // Held while scrolling to swap the hotbars.
    public static boolean scrollModifierDown() {
        return scrollModifier != null && scrollModifier.isDown();
    }

    // Called by each loader at the end of every client tick. Presses only count while no screen is open.
    public static void tick(Minecraft minecraft) {
        SecondBarSelection.tick(minecraft);
        if (swap == null) {
            return;
        }
        while (swap.consumeClick()) {
            // A key press turns the wheel the same way as scrolling down.
            HotbarSwapper.swap(minecraft, true);
        }
        while (openSettings.consumeClick()) {
            DualHotbar.setScreen(new DualHotbarSettingsScreen(null));
        }
    }
}
