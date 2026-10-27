package io.github.rebelkeithy.dualhotbar;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
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
    // Room for the overlap warning (one line) and the compatibility note (three lines) above the Done button.
    private static final int FOOTER_HEIGHT = 77;
    // With the second bar selectable, an overlap above half its height hides most of what you'd select.
    private static final int WARNING_OVERLAP = DualHotbarConfig.MAX_OVERLAP / 2 + 1;
    private static final Component OVERLAP_WARNING = Component.translatable("dualhotbar.settings.overlapWarning").withStyle(ChatFormatting.YELLOW);

    // The options that only matter when the second bar is selectable, greyed out otherwise.
    private final OptionInstance<Boolean> scrollPastEdge = toggle("scrollPastEdge", DualHotbarConfig.scrollPastEdge(), DualHotbarConfig::setScrollPastEdge);
    private final OptionInstance<Boolean> doubleTapNumberKeys = toggle("doubleTapNumberKeys", DualHotbarConfig.doubleTapNumberKeys(), DualHotbarConfig::setDoubleTapNumberKeys);
    private @Nullable StringWidget overlapWarning;

    public DualHotbarSettingsScreen(@Nullable Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Component.translatable("dualhotbar.settings.title"));
    }

    @Override
    protected void addOptions() {
        this.list.addSmall(scale(), overlap(), animation(), scrollToSwap());
        this.list.addSmall(
                Button.builder(Component.translatable("dualhotbar.options.keyBinds"),
                        button -> DualHotbar.setScreen(new KeyBindsScreen(this, this.options))).build(),
                Button.builder(Component.translatable("dualhotbar.options.reset"), button -> resetToDefaults()).build()
        );
        this.list.addHeader(Component.translatable("dualhotbar.options.selectionHeader"));
        OptionInstance<Boolean> selectable = toggle("selectableSecondBar", DualHotbarConfig.selectableSecondBar(), enabled -> {
            DualHotbarConfig.setSelectableSecondBar(enabled);
            updateSelectionOptions();
        });
        this.list.addSmall(selectable, this.scrollPastEdge, this.doubleTapNumberKeys);
    }

    // Done, with the overlap warning and the note about selecting the second bar above it.
    @Override
    protected void addFooter() {
        this.layout.setFooterHeight(FOOTER_HEIGHT);
        LinearLayout footer = this.layout.addToFooter(LinearLayout.vertical().spacing(6));
        footer.defaultCellSetting().alignHorizontallyCenter();
        // Sized for the warning and left empty when it doesn't apply, so the layout doesn't move.
        this.overlapWarning = footer.addChild(new StringWidget(this.font.width(OVERLAP_WARNING), this.font.lineHeight, CommonComponents.EMPTY, this.font));
        footer.addChild(new MultiLineTextWidget(Component.translatable("dualhotbar.settings.selectionNote").withStyle(ChatFormatting.GRAY), this.font)
                .setMaxWidth(310)
                .setCentered(true));
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
    }

    @Override
    protected void init() {
        super.init();
        updateSelectionOptions();
    }

    private void updateSelectionOptions() {
        boolean selectable = DualHotbarConfig.selectableSecondBar();
        for (OptionInstance<Boolean> option : java.util.List.of(this.scrollPastEdge, this.doubleTapNumberKeys)) {
            AbstractWidget widget = this.list == null ? null : this.list.findOption(option);
            if (widget != null) {
                widget.active = selectable;
            }
        }
        if (this.overlapWarning != null) {
            boolean warn = selectable && DualHotbarConfig.secondHotbarOverlap() >= WARNING_OVERLAP;
            this.overlapWarning.setMessage(warn ? OVERLAP_WARNING : CommonComponents.EMPTY);
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
        // A fresh screen shows the new values; closing this one saves them.
        DualHotbar.setScreen(new DualHotbarSettingsScreen(this.lastScreen));
    }

    // 10% to 100% in steps of 5%.
    private static OptionInstance<Integer> scale() {
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
