package io.github.rebelkeithy.dualhotbar;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.minecraft.client.Minecraft;

// Settings in config/dualhotbar.properties, also edited by DualHotbarSettingsScreen. The file is written with defaults
// on first use and re-read when it changes (checked at most once a second), so hand edits apply in game too.
public final class DualHotbarConfig {
    public static final float DEFAULT_SCALE = 0.5F;
    public static final int DEFAULT_OVERLAP = 3;
    // The second bar is 22 of its own pixels tall, so this tucks it completely behind the hotbar.
    public static final int MAX_OVERLAP = 22;
    public static final int DEFAULT_ANIMATION_MILLIS = 300;
    public static final int MAX_ANIMATION_MILLIS = 5000;
    public static final boolean DEFAULT_SCROLL_TO_SWAP = true;
    public static final boolean DEFAULT_SELECTABLE_SECOND_BAR = false;
    public static final boolean DEFAULT_SCROLL_PAST_EDGE = true;
    public static final boolean DEFAULT_DOUBLE_TAP_NUMBER_KEYS = true;
    private static final long CHECK_INTERVAL_NANOS = 1_000_000_000L;

    private static float secondHotbarScale = DEFAULT_SCALE;
    private static int secondHotbarOverlap = DEFAULT_OVERLAP;
    private static int swapAnimationMillis = DEFAULT_ANIMATION_MILLIS;
    private static boolean scrollToSwap = DEFAULT_SCROLL_TO_SWAP;
    private static boolean selectableSecondBar = DEFAULT_SELECTABLE_SECOND_BAR;
    private static boolean scrollPastEdge = DEFAULT_SCROLL_PAST_EDGE;
    private static boolean doubleTapNumberKeys = DEFAULT_DOUBLE_TAP_NUMBER_KEYS;
    private static long loadedModifiedTime = Long.MIN_VALUE;
    private static long nextCheck = System.nanoTime();
    private static boolean checked;

    private DualHotbarConfig() {
    }

    public static float secondHotbarScale() {
        reloadIfChanged();
        return secondHotbarScale;
    }

    public static void setSecondHotbarScale(float scale) {
        secondHotbarScale = Math.clamp(scale, 0.1F, 1.0F);
    }

    // Measured in the second bar's own pixels, so the look stays the same at any scale. 3 hides its bottom border, and
    // MAX_OVERLAP hides the whole bar until a swap.
    public static int secondHotbarOverlap() {
        reloadIfChanged();
        return secondHotbarOverlap;
    }

    public static void setSecondHotbarOverlap(int overlap) {
        secondHotbarOverlap = Math.clamp(overlap, 0, MAX_OVERLAP);
    }

    public static int swapAnimationMillis() {
        reloadIfChanged();
        return swapAnimationMillis;
    }

    public static void setSwapAnimationMillis(int millis) {
        swapAnimationMillis = Math.clamp(millis, 0, MAX_ANIMATION_MILLIS);
    }

    public static boolean scrollToSwap() {
        reloadIfChanged();
        return scrollToSwap;
    }

    public static void setScrollToSwap(boolean enabled) {
        scrollToSwap = enabled;
    }

    // Whether the selection can move onto the second bar (SecondBarSelection), by the two ways below.
    public static boolean selectableSecondBar() {
        reloadIfChanged();
        return selectableSecondBar;
    }

    public static void setSelectableSecondBar(boolean enabled) {
        selectableSecondBar = enabled;
    }

    public static boolean scrollPastEdge() {
        reloadIfChanged();
        return scrollPastEdge;
    }

    public static void setScrollPastEdge(boolean enabled) {
        scrollPastEdge = enabled;
    }

    public static boolean doubleTapNumberKeys() {
        reloadIfChanged();
        return doubleTapNumberKeys;
    }

    public static void setDoubleTapNumberKeys(boolean enabled) {
        doubleTapNumberKeys = enabled;
    }

    // Writes the current settings to the file.
    public static void save() {
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, contents());
            loadedModifiedTime = Files.getLastModifiedTime(file).toMillis();
        } catch (IOException e) {
            DualHotbar.LOGGER.warn("Couldn't save {}", file, e);
        }
    }

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(DualHotbar.MOD_ID + ".properties");
    }

    private static String contents() {
        return """
                # Dual Hotbar settings, also editable in game: the mod's config button in the Mods list or Mod Menu,
                # or the "Open Dual Hotbar settings" key (unbound by default). Changes here apply while the game runs.

                # Size of the second hotbar relative to the normal hotbar, from 0.1 to 1.0.
                secondHotbarScale=%s

                # How many of the second hotbar's own pixels are tucked behind the top of the hotbar, from 0 to %d.
                # 3 hides its bottom border; %2$d hides it completely, leaving the HUD where vanilla puts it.
                secondHotbarOverlap=%d

                # Length of the swap animation in milliseconds, up to %d. 0 swaps instantly.
                swapAnimationMillis=%d

                # Whether scrolling while holding the swap modifier key (Shift by default) swaps the hotbars.
                scrollToSwap=%s

                # Experimental: whether the selection can move onto the second hotbar. Its item is held by swapping it
                # into the hotbar slot below, so other mods, servers and container screens see the two swapped.
                selectableSecondHotbar=%s
                # With the above: scrolling past either end of a hotbar moves onto the other one.
                scrollPastEdge=%s
                # With the above: pressing a number key twice quickly selects that slot of the second hotbar.
                doubleTapNumberKeys=%s
                """.formatted(secondHotbarScale, MAX_OVERLAP, secondHotbarOverlap, MAX_ANIMATION_MILLIS, swapAnimationMillis, scrollToSwap,
                selectableSecondBar, scrollPastEdge, doubleTapNumberKeys);
    }

    private static void reloadIfChanged() {
        long now = System.nanoTime();
        if (checked && now - nextCheck < 0) {
            return;
        }
        checked = true;
        nextCheck = now + CHECK_INTERVAL_NANOS;

        Path file = file();
        try {
            if (!Files.exists(file)) {
                save();
                return;
            }
            long modifiedTime = Files.getLastModifiedTime(file).toMillis();
            if (modifiedTime == loadedModifiedTime) {
                return;
            }
            loadedModifiedTime = modifiedTime;
            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(file)) {
                properties.load(reader);
            }
            setSecondHotbarScale(readFloat(properties, "secondHotbarScale", DEFAULT_SCALE));
            setSecondHotbarOverlap(readInt(properties, "secondHotbarOverlap", DEFAULT_OVERLAP));
            setSwapAnimationMillis(readInt(properties, "swapAnimationMillis", DEFAULT_ANIMATION_MILLIS));
            setScrollToSwap(readBoolean(properties, "scrollToSwap", DEFAULT_SCROLL_TO_SWAP));
            setSelectableSecondBar(readBoolean(properties, "selectableSecondHotbar", DEFAULT_SELECTABLE_SECOND_BAR));
            setScrollPastEdge(readBoolean(properties, "scrollPastEdge", DEFAULT_SCROLL_PAST_EDGE));
            setDoubleTapNumberKeys(readBoolean(properties, "doubleTapNumberKeys", DEFAULT_DOUBLE_TAP_NUMBER_KEYS));
        } catch (IOException e) {
            DualHotbar.LOGGER.warn("Couldn't read {}; using the current settings", file, e);
        }
    }

    private static float readFloat(Properties properties, String key, float fallback) {
        String value = properties.getProperty(key, "").trim();
        try {
            return value.isEmpty() ? fallback : Float.parseFloat(value);
        } catch (NumberFormatException e) {
            DualHotbar.LOGGER.warn("{} must be a number, not '{}'; using {}", key, value, fallback);
            return fallback;
        }
    }

    private static int readInt(Properties properties, String key, int fallback) {
        String value = properties.getProperty(key, "").trim();
        try {
            return value.isEmpty() ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            DualHotbar.LOGGER.warn("{} must be a whole number, not '{}'; using {}", key, value, fallback);
            return fallback;
        }
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key, "").trim();
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        if (!value.isEmpty()) {
            DualHotbar.LOGGER.warn("{} must be true or false, not '{}'; using {}", key, value, fallback);
        }
        return fallback;
    }
}
