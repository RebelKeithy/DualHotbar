package io.github.rebelkeithy.dualhotbar;

import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.joml.Matrix3x2fStack;

// Draws the hotbar and the second hotbar in place of vanilla's item hotbar. At rest the hotbar, offhand slot and
// attack indicator are exactly where vanilla puts them; the second bar sits above the hotbar with its bottom edge (by
// default) tucked behind it, and the HUD elements that normally sit on the hotbar (health, food, experience, ...) are moved up by
// decorationShift() by each loader.
public final class HotbarRenderer {
    private static final Identifier HOTBAR_SPRITE = Identifier.withDefaultNamespace("hud/hotbar");
    private static final Identifier HOTBAR_SELECTION_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_selection");
    private static final Identifier HOTBAR_OFFHAND_LEFT_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_offhand_left");
    private static final Identifier HOTBAR_OFFHAND_RIGHT_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_offhand_right");
    private static final Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_background");
    private static final Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE = Identifier.withDefaultNamespace("hud/hotbar_attack_indicator_progress");
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 22;
    private static final int SLOT_WIDTH = 20;
    private static final int SLOTS = Inventory.getSelectionSize();
    // The Long Hotbar: 18 slots between the two outer borders, and the room kept beside it for the offhand slot.
    private static final int LONG_BAR_WIDTH = 2 * SLOTS * SLOT_WIDTH + 2;
    private static final int OFFHAND_ROOM = 31;
    // Mid-swap, the bars' centres are at least a hotbar height apart (each moves half a hotbar height off the middle),
    // even when they rest on top of each other.
    private static final float MIN_LIFT = BAR_HEIGHT / 2.0F;
    // Item seeds as vanilla numbers them: hotbar 1-9, offhand 10. The second bar continues from 11.
    private static final int OFFHAND_SEED = 10;
    private static final int SECOND_BAR_SEED = 11;

    private HotbarRenderer() {
    }

    public static void extractHotbars(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.getCameraEntity() instanceof Player player)) {
            return;
        }
        if (DualHotbarConfig.longHotbarInUse()) {
            extractLongHotbar(graphics, deltaTracker, minecraft, player);
            return;
        }

        float secondScale = DualHotbarConfig.secondHotbarScale();
        float hotbarCenter = BAR_HEIGHT / 2.0F;
        float secondCenter = secondBarBottom(secondScale) + BAR_HEIGHT * secondScale / 2.0F;
        double lag = SwapAnimation.lag();
        SwapAnimation.Pose hotbar = SwapAnimation.pose(SwapAnimation.HOTBAR_ANGLE - lag, secondScale, hotbarCenter, secondCenter, MIN_LIFT);
        SwapAnimation.Pose second = SwapAnimation.pose(SwapAnimation.SECOND_BAR_ANGLE - lag, secondScale, hotbarCenter, secondCenter, MIN_LIFT);

        Placement hotbarPlacement = place(graphics, minecraft, hotbar);
        Placement secondPlacement = place(graphics, minecraft, second);

        int screenCenter = graphics.guiWidth() / 2;
        extractOffhandAndAttackIndicator(graphics, deltaTracker, minecraft, player, screenCenter - BAR_WIDTH / 2, screenCenter + BAR_WIDTH / 2);
        // The selection frame goes on the bar the held item is shown in: the hotbar's is part of the hotbar, as in
        // vanilla, and the second bar's is drawn last, over the hotbar, so it isn't cut off where the rest of the
        // second bar is.
        int secondColumn = SecondBarSelection.selectedColumn();
        int hotbarFrame = secondColumn < 0 ? player.getInventory().getSelectedSlot() : -1;
        // The bigger bar is at the front of the wheel, so it's drawn last. At rest that puts the second bar behind.
        if (hotbar.scale() >= second.scale()) {
            extractBehind(graphics, secondPlacement, hotbarPlacement,
                    () -> extractBar(graphics, deltaTracker, minecraft, player, secondPlacement, true, -1, SECOND_BAR_SEED));
            extractBar(graphics, deltaTracker, minecraft, player, hotbarPlacement, false, hotbarFrame, 1);
        } else {
            extractBehind(graphics, hotbarPlacement, secondPlacement,
                    () -> extractBar(graphics, deltaTracker, minecraft, player, hotbarPlacement, false, hotbarFrame, 1));
            extractBar(graphics, deltaTracker, minecraft, player, secondPlacement, true, -1, SECOND_BAR_SEED);
        }
        if (secondColumn >= 0) {
            inBar(graphics, secondPlacement, () -> extractSelectionFrame(graphics, secondColumn));
            // As in vanilla's hotbar, the frame sits under the selected item and its count, so they're drawn again on
            // top of it, cut off by the hotbar like the rest of the second bar.
            ItemStack selected = player.getInventory().getItem(SecondBarSelection.displaySlot(true, secondColumn));
            extractBehind(graphics, secondPlacement, hotbarPlacement, () -> inBar(graphics, secondPlacement,
                    () -> extractSlot(graphics, deltaTracker, minecraft, player, selected, 3 + secondColumn * 20, 3, SECOND_BAR_SEED + secondColumn)));
        }
    }

    // Long Hotbar: the hotbar and the second bar side by side as one 18-slot bar, the hotbar on the left, at full
    // size unless the screen is too narrow, with room for the offhand slot either side. Nothing sits above it, so the
    // HUD stays at vanilla's height. Selection works as in the stacked bars (SecondBarSelection); the frame is simply
    // on whichever of the 18 slots is selected. The bars don't swap in this mode (HotbarSwapper), so it behaves like a
    // plain 18-slot hotbar.
    private static void extractLongHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft, Player player) {
        int guiScale = minecraft.getWindow().getGuiScale();
        float scale = Math.min(1.0F, (graphics.guiWidth() - 2.0F * OFFHAND_ROOM) / LONG_BAR_WIDTH);
        float left = snap(graphics.guiWidth() / 2.0F - LONG_BAR_WIDTH * scale / 2.0F, guiScale);
        float top = snap(graphics.guiHeight() - BAR_HEIGHT * scale, guiScale);
        extractOffhandAndAttackIndicator(graphics, deltaTracker, minecraft, player, Mth.floor(left), Mth.ceil(left + LONG_BAR_WIDTH * scale));

        Inventory inventory = player.getInventory();
        int secondColumn = SecondBarSelection.selectedColumn();
        int frameSlot = secondColumn >= 0 ? SLOTS + secondColumn : inventory.getSelectedSlot();
        inBar(graphics, new Placement(left, top, scale), () -> {
            extractLongBarSprite(graphics);
            extractSelectionFrame(graphics, frameSlot);
            for (int slot = 0; slot < 2 * SLOTS; slot++) {
                boolean secondBar = slot >= SLOTS;
                int column = slot % SLOTS;
                ItemStack itemStack = inventory.getItem(SecondBarSelection.displaySlot(secondBar, column));
                extractSlot(graphics, deltaTracker, minecraft, player, itemStack, 3 + slot * SLOT_WIDTH, 3, (secondBar ? SECOND_BAR_SEED : 1) + column);
            }
        });
    }

    // The 18-slot bar, built from pieces of the 9-slot sprite so every join is an ordinary divider between slots: its
    // left border and first 9 slots, an inner slot again for slot 10, then its last 8 slots and right border.
    private static void extractLongBarSprite(GuiGraphicsExtractor graphics) {
        int nineSlots = SLOTS * SLOT_WIDTH;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, BAR_WIDTH, BAR_HEIGHT, 0, 0, 0, 0, nineSlots, BAR_HEIGHT);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, BAR_WIDTH, BAR_HEIGHT, SLOT_WIDTH, 0, nineSlots, 0, SLOT_WIDTH, BAR_HEIGHT);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, BAR_WIDTH, BAR_HEIGHT, SLOT_WIDTH, 0, nineSlots + SLOT_WIDTH, 0,
                BAR_WIDTH - SLOT_WIDTH, BAR_HEIGHT);
    }

    // How far the HUD elements stacked on the hotbar move up to make room for the second bar. 0 in spectator mode,
    // which shows the spectator menu instead of the hotbars, and for the Long Hotbar, which has nothing above it.
    public static int decorationShift() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR || DualHotbarConfig.longHotbarInUse()) {
            return 0;
        }
        float secondScale = DualHotbarConfig.secondHotbarScale();
        return Mth.ceil(secondBarBottom(secondScale) + BAR_HEIGHT * secondScale) - BAR_HEIGHT;
    }

    // Height of the resting second bar's bottom edge above the bottom of the screen, in GUI pixels. The overlap is in
    // the second bar's own pixels: the default 3 tucks its bottom border, the rows below the slots, behind the hotbar,
    // and the full 22 tucks the whole bar behind it, which also leaves the HUD above at vanilla's height.
    private static float secondBarBottom(float secondScale) {
        return BAR_HEIGHT - DualHotbarConfig.secondHotbarOverlap() * secondScale;
    }

    // Runs extract with everything it draws moved up by shift GUI pixels.
    public static void extractShiftedUp(GuiGraphicsExtractor graphics, int shift, Runnable extract) {
        if (shift == 0) {
            extract.run();
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0F, -shift);
        extract.run();
        graphics.pose().popMatrix();
    }

    // Where a bar is drawn: its top-left corner in GUI pixels and its scale.
    private record Placement(float left, float top, float scale) {
        float bottom() {
            return this.top + BAR_HEIGHT * this.scale;
        }
    }

    private static Placement place(GuiGraphicsExtractor graphics, Minecraft minecraft, SwapAnimation.Pose pose) {
        float scale = pose.scale();
        int guiScale = minecraft.getWindow().getGuiScale();
        // Snapped to whole screen pixels so the scaled bar stays crisp. At scale 1 this is vanilla's hotbar position.
        float left = snap(graphics.guiWidth() / 2 - BAR_WIDTH * scale / 2.0F, guiScale);
        float top = snap(graphics.guiHeight() - pose.centerHeight() - BAR_HEIGHT * scale / 2.0F, guiScale);
        return new Placement(left, top, scale);
    }

    // Draws the back bar only where the front bar doesn't cover it, so it can't show through the front bar's
    // see-through slots. The front bar is the bigger one and both are centred, so that leaves the strips above and
    // below it. Rounding the strips to whole GUI pixels only reaches into the front bar's opaque top and bottom edges.
    private static void extractBehind(GuiGraphicsExtractor graphics, Placement back, Placement front, Runnable extractBack) {
        int frontTop = Mth.ceil(front.top());
        int frontBottom = Mth.floor(front.bottom());
        if (back.top() < frontTop) {
            graphics.enableScissor(0, 0, graphics.guiWidth(), frontTop);
            extractBack.run();
            graphics.disableScissor();
        }
        if (back.bottom() > frontBottom) {
            graphics.enableScissor(0, frontBottom, graphics.guiWidth(), graphics.guiHeight());
            extractBack.run();
            graphics.disableScissor();
        }
    }

    // A bar's sprite, selection frame (frameColumn, or -1 for none) and items.
    private static void extractBar(
            GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft, Player player, Placement placement,
            boolean secondBar, int frameColumn, int seed
    ) {
        Inventory inventory = player.getInventory();
        inBar(graphics, placement, () -> {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 0, 0, BAR_WIDTH, BAR_HEIGHT);
            if (frameColumn >= 0) {
                extractSelectionFrame(graphics, frameColumn);
            }
            for (int column = 0; column < Inventory.getSelectionSize(); column++) {
                ItemStack itemStack = inventory.getItem(SecondBarSelection.displaySlot(secondBar, column));
                extractSlot(graphics, deltaTracker, minecraft, player, itemStack, 3 + column * 20, 3, seed + column);
            }
        });
    }

    // Runs extract in the bar's own coordinates: its top-left corner at 0, 0 and its full 182 x 22 size.
    private static void inBar(GuiGraphicsExtractor graphics, Placement placement, Runnable extract) {
        Matrix3x2fStack matrices = graphics.pose();
        matrices.pushMatrix();
        matrices.translate(placement.left(), placement.top());
        matrices.scale(placement.scale(), placement.scale());
        extract.run();
        matrices.popMatrix();
    }

    private static void extractSelectionFrame(GuiGraphicsExtractor graphics, int column) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_SPRITE, -1 + column * 20, -1, 24, 23);
    }

    // The offhand slot and attack indicator, beside the hotbar's left and right edges (barLeft, barRight) as in
    // vanilla. They stay put while the bars swap.
    private static void extractOffhandAndAttackIndicator(
            GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft, Player player, int barLeft, int barRight
    ) {
        int bottom = graphics.guiHeight();
        ItemStack offhand = player.getOffhandItem();
        HumanoidArm offhandArm = player.getMainArm().getOpposite();
        if (!offhand.isEmpty()) {
            if (offhandArm == HumanoidArm.LEFT) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, barLeft - 29, bottom - 23, 29, 24);
                extractSlot(graphics, deltaTracker, minecraft, player, offhand, barLeft - 26, bottom - 16 - 3, OFFHAND_SEED);
            } else {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_RIGHT_SPRITE, barRight, bottom - 23, 29, 24);
                extractSlot(graphics, deltaTracker, minecraft, player, offhand, barRight + 10, bottom - 16 - 3, OFFHAND_SEED);
            }
        }

        if (minecraft.player != null && minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
            float attackStrengthScale = minecraft.player.getAttackStrengthScale(0.0F);
            if (attackStrengthScale < 1.0F) {
                int x = offhandArm == HumanoidArm.RIGHT ? barLeft - 22 : barRight + 6;
                int y = bottom - 20;
                int progress = (int) (attackStrengthScale * 19.0F);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, x, y, 18, 18);
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED, HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - progress, x, y + 18 - progress, 18, progress
                );
            }
        }
    }

    // An item in a hotbar slot, squeezed briefly after it's picked up like vanilla does.
    private static void extractSlot(
            GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft, Player player, ItemStack itemStack, int x, int y, int seed
    ) {
        if (itemStack.isEmpty()) {
            return;
        }
        float pop = itemStack.getPopTime() - deltaTracker.getGameTimeDeltaPartialTick(false);
        if (pop > 0.0F) {
            float squeeze = 1.0F + pop / 5.0F;
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + 8, y + 12);
            graphics.pose().scale(1.0F / squeeze, (squeeze + 1.0F) / 2.0F);
            graphics.pose().translate(-(x + 8), -(y + 12));
        }
        graphics.item(player, itemStack, x, y, seed);
        if (pop > 0.0F) {
            graphics.pose().popMatrix();
        }
        graphics.itemDecorations(minecraft.font, itemStack, x, y);
    }

    private static float snap(float guiPixels, int guiScale) {
        return Math.round(guiPixels * guiScale) / (float) guiScale;
    }
}
