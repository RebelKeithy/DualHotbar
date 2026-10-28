package io.github.rebelkeithy.dualhotbar;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

// The in-game settings page, in the style of vanilla's option screens. Opened from the Mods list (NeoForge), Mod Menu
// (Fabric) or the "Open Dual Hotbar Settings" key. Changes apply at once and are saved when the screen closes.
public class DualHotbarSettingsScreen extends OptionsSubScreen {
    private static final int SCALE_STEP_PERCENT = 5;
    private static final int ANIMATION_STEP_MILLIS = 25;
    private static final int ANIMATION_SLIDER_MAX_MILLIS = 1000;
    // A row of two option buttons, relative to the middle of the screen, which the notes in the list line up with.
    private static final int ROW_LEFT = -155;
    private static final int ROW_WIDTH = 310;
    private static final int RIGHT_BUTTON_LEFT = 5;
    private static final int BUTTON_WIDTH = 150;
    // With the second bar selectable, an overlap above half its height hides most of what you'd select.
    private static final int WARNING_OVERLAP = DualHotbarConfig.MAX_OVERLAP / 2 + 1;
    private static final Component OVERLAP_WARNING = Component.translatable("dualhotbar.settings.overlapWarning").withStyle(ChatFormatting.YELLOW);

    // The options that only matter when the second bar is selectable, greyed out otherwise.
    private final OptionInstance<Boolean> scrollPastEdge = toggle("scrollPastEdge", DualHotbarConfig.scrollPastEdge(), DualHotbarConfig::setScrollPastEdge);
    private final OptionInstance<Boolean> doubleTapNumberKeys = toggle("doubleTapNumberKeys", DualHotbarConfig.doubleTapNumberKeys(), DualHotbarConfig::setDoubleTapNumberKeys);
    private final OptionInstance<Boolean> longHotbar = toggle("longHotbar", DualHotbarConfig.longHotbar(), enabled -> {
        DualHotbarConfig.setLongHotbar(enabled);
        updateSelectionOptions();
    });
    // The options that don't apply to the Long Hotbar, greyed out while it's in use: it has no second bar to size or
    // tuck, and its two halves never swap.
    private final OptionInstance<Integer> scale = scale();
    private final OptionInstance<Integer> overlap = overlap();
    private final OptionInstance<Integer> animation = animation();
    private final OptionInstance<Boolean> scrollToSwap = scrollToSwap();
    private SettingsList.@Nullable TextEntry overlapWarning;

    public DualHotbarSettingsScreen(@Nullable Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Component.translatable("dualhotbar.settings.title"));
    }

    @Override
    protected void addOptions() {
        SettingsList list = (SettingsList) this.list;
        this.list.addSmall(this.scale, this.overlap);
        // Under the Overlap slider.
        this.overlapWarning = list.addText(OVERLAP_WARNING, RIGHT_BUTTON_LEFT, BUTTON_WIDTH);
        this.list.addSmall(this.animation, this.scrollToSwap);
        this.list.addSmall(
                Button.builder(Component.translatable("dualhotbar.options.keyBinds"),
                        button -> DualHotbar.setScreen(new KeyBindsScreen(this, this.options))).build(),
                Button.builder(Component.translatable("dualhotbar.options.reset"), button -> resetToDefaults()).build()
        );
        this.list.addHeader(Component.translatable("dualhotbar.options.selectionHeader"));
        list.addText(Component.translatable("dualhotbar.settings.selectionNote").withStyle(ChatFormatting.GRAY), ROW_LEFT, ROW_WIDTH);
        OptionInstance<Boolean> selectable = toggle("selectableSecondBar", DualHotbarConfig.selectableSecondBar(), enabled -> {
            DualHotbarConfig.setSelectableSecondBar(enabled);
            updateSelectionOptions();
        });
        this.list.addSmall(selectable, this.scrollPastEdge, this.doubleTapNumberKeys, this.longHotbar);
    }

    // The vanilla options list, which can also hold wrapped text.
    @Override
    protected void addContents() {
        this.list = this.layout.addToContents(new SettingsList(this.minecraft, this.width, this));
        this.addOptions();
    }

    @Override
    protected void init() {
        super.init();
        updateSelectionOptions();
    }

    private void updateSelectionOptions() {
        boolean selectable = DualHotbarConfig.selectableSecondBar();
        boolean longHotbarInUse = DualHotbarConfig.longHotbarInUse();
        setActive(this.scrollPastEdge, selectable);
        setActive(this.doubleTapNumberKeys, selectable);
        setActive(this.longHotbar, selectable);
        for (OptionInstance<?> option : java.util.List.of(this.scale, this.overlap, this.animation, this.scrollToSwap)) {
            setActive(option, !longHotbarInUse);
        }
        if (this.overlapWarning != null) {
            boolean warn = selectable && !longHotbarInUse && DualHotbarConfig.secondHotbarOverlap() >= WARNING_OVERLAP;
            this.overlapWarning.setText(warn ? OVERLAP_WARNING : CommonComponents.EMPTY);
        }
    }

    private static final class SettingsList extends OptionsList {
        private static final int TEXT_PADDING = 2;

        private final Screen screen;

        private SettingsList(Minecraft minecraft, int width, OptionsSubScreen screen) {
            super(minecraft, width, screen);
            this.screen = screen;
        }

        // A row of text wrapped to, and centred on, the part of a row of buttons `left` to `left + width` from the
        // middle of the screen.
        private TextEntry addText(Component text, int left, int width) {
            TextEntry entry = new TextEntry(this, text, left, width);
            this.addEntry(entry, entry.rowHeight());
            return entry;
        }

        private static final class TextEntry extends OptionsList.AbstractEntry {
            private final SettingsList settingsList;
            private final MultiLineTextWidget widget;
            private final int left;
            private final int width;

            private TextEntry(SettingsList settingsList, Component text, int left, int width) {
                this.settingsList = settingsList;
                this.widget = new MultiLineTextWidget(text, settingsList.screen.getFont()).setMaxWidth(width).setCentered(true);
                this.left = left;
                this.width = width;
            }

            // Changes the text, and the row's height to fit it: with no text the row takes no room.
            private void setText(Component text) {
                this.widget.setMessage(text);
                this.setHeight(rowHeight());
                // Lays the rows below this one out again.
                this.settingsList.setScrollAmount(this.settingsList.scrollAmount());
            }

            private int rowHeight() {
                return this.widget.getMessage().getString().isEmpty() ? 0 : this.widget.getHeight() + 2 * (CONTENT_PADDING + TEXT_PADDING);
            }

            @Override
            public java.util.List<? extends NarratableEntry> narratables() {
                return java.util.List.of(this.widget);
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
                int middle = this.settingsList.screen.width / 2 + this.left + this.width / 2;
                this.widget.setPosition(middle - this.widget.getWidth() / 2, this.getContentY() + TEXT_PADDING);
                this.widget.extractRenderState(graphics, mouseX, mouseY, a);
            }

            @Override
            public java.util.List<? extends GuiEventListener> children() {
                return java.util.List.of(this.widget);
            }
        }
    }

    private void setActive(OptionInstance<?> option, boolean active) {
        AbstractWidget widget = this.list == null ? null : this.list.findOption(option);
        if (widget != null) {
            widget.active = active;
        }
    }

    @Override
    public void removed() {
        DualHotbarConfig.save();
        super.removed();
    }

    private void resetToDefaults() {
        DualHotbarConfig.setSecondHotbarScale(DualHotbarConfig.DEFAULT_SCALE);
        DualHotbarConfig.setSecondHotbarOverlap(DualHotbarConfig.DEFAULT_OVERLAP);
        DualHotbarConfig.setSwapAnimationMillis(DualHotbarConfig.DEFAULT_ANIMATION_MILLIS);
        DualHotbarConfig.setScrollToSwap(DualHotbarConfig.DEFAULT_SCROLL_TO_SWAP);
        DualHotbarConfig.setSelectableSecondBar(DualHotbarConfig.DEFAULT_SELECTABLE_SECOND_BAR);
        DualHotbarConfig.setScrollPastEdge(DualHotbarConfig.DEFAULT_SCROLL_PAST_EDGE);
        DualHotbarConfig.setDoubleTapNumberKeys(DualHotbarConfig.DEFAULT_DOUBLE_TAP_NUMBER_KEYS);
        DualHotbarConfig.setLongHotbar(DualHotbarConfig.DEFAULT_LONG_HOTBAR);
        // A fresh screen shows the new values; closing this one saves them.
        DualHotbar.setScreen(new DualHotbarSettingsScreen(this.lastScreen));
    }

    // 10% to 100% in steps of 5%.
    private OptionInstance<Integer> scale() {
        return new OptionInstance<>(
                "dualhotbar.options.scale",
                OptionInstance.cachedConstantTooltip(Component.translatable("dualhotbar.options.scale.tooltip")),
                (caption, steps) -> Component.translatable("options.percent_value", caption, steps * SCALE_STEP_PERCENT),
                new OptionInstance.IntRange(10 / SCALE_STEP_PERCENT, 100 / SCALE_STEP_PERCENT, true),
                Math.round(DualHotbarConfig.secondHotbarScale() * 100 / SCALE_STEP_PERCENT),
                steps -> DualHotbarConfig.setSecondHotbarScale(steps * SCALE_STEP_PERCENT / 100.0F)
        );
    }

    private OptionInstance<Integer> overlap() {
        return new OptionInstance<>(
                "dualhotbar.options.overlap",
                OptionInstance.cachedConstantTooltip(Component.translatable("dualhotbar.options.overlap.tooltip")),
                (caption, pixels) -> pixels == DualHotbarConfig.MAX_OVERLAP
                        ? Component.translatable("options.generic_value", caption, Component.translatable("dualhotbar.options.overlap.full"))
                        : Component.translatable("options.pixel_value", caption, pixels),
                new OptionInstance.IntRange(0, DualHotbarConfig.MAX_OVERLAP, true),
                DualHotbarConfig.secondHotbarOverlap(),
                pixels -> {
                    DualHotbarConfig.setSecondHotbarOverlap(pixels);
                    updateSelectionOptions();
                }
        );
    }

    // Off, then up to a second in steps of 25 ms. Longer times can still be set in the config file.
    private static OptionInstance<Integer> animation() {
        return new OptionInstance<>(
                "dualhotbar.options.animation",
                OptionInstance.cachedConstantTooltip(Component.translatable("dualhotbar.options.animation.tooltip")),
                (caption, steps) -> steps == 0
                        ? Component.translatable("options.generic_value", caption, CommonComponents.OPTION_OFF)
                        : Component.translatable("dualhotbar.options.milliseconds", caption, steps * ANIMATION_STEP_MILLIS),
                new OptionInstance.IntRange(0, ANIMATION_SLIDER_MAX_MILLIS / ANIMATION_STEP_MILLIS, true),
                Math.min(Math.round(DualHotbarConfig.swapAnimationMillis() / (float) ANIMATION_STEP_MILLIS), ANIMATION_SLIDER_MAX_MILLIS / ANIMATION_STEP_MILLIS),
                steps -> DualHotbarConfig.setSwapAnimationMillis(steps * ANIMATION_STEP_MILLIS)
        );
    }

    private static OptionInstance<Boolean> scrollToSwap() {
        return toggle("scrollToSwap", DualHotbarConfig.scrollToSwap(), DualHotbarConfig::setScrollToSwap);
    }

    // An on/off option named dualhotbar.options.<name>, with its tooltip at <name>.tooltip.
    private static OptionInstance<Boolean> toggle(String name, boolean value, java.util.function.Consumer<Boolean> onChange) {
        return OptionInstance.createBoolean(
                "dualhotbar.options." + name,
                OptionInstance.cachedConstantTooltip(Component.translatable("dualhotbar.options." + name + ".tooltip")),
                value,
                onChange::accept
        );
    }
}
