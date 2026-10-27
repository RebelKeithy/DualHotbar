package io.github.rebelkeithy.dualhotbar.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.rebelkeithy.dualhotbar.DualHotbarSettingsScreen;

// Mod Menu's config button for Dual Hotbar, listed under "modmenu" in fabric.mod.json. Mod Menu is optional: without
// it, Fabric never loads this class.
public class DualHotbarModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return DualHotbarSettingsScreen::new;
    }
}
