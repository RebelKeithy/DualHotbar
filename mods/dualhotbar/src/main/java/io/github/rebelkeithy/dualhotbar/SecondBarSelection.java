package io.github.rebelkeithy.dualhotbar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;

// Lets the selection move onto the second bar (experimental, off by default). Only hotbar slots can be held, and a
// vanilla server refuses anything else, so while second-bar column N is selected its item is swapped into hotbar slot
// N and the hotbar's own item into the second bar's slot (one "swap with hotbar slot" click), and hotbar slot N is
// selected. The renderer draws every item where it belongs (displaySlot). Selecting anything else swaps the column
// back. Other mods, servers and container screens see the two items swapped while this lasts.
//
// The swap is undone while the pause menu or the player's own inventory is open, so quitting saves the real layout and
// the inventory shows it, and is redone when the screen closes if nothing else was selected meanwhile.
public final class SecondBarSelection {
    private static final long DOUBLE_TAP_NANOS = 300_000_000L;

    // The second-bar column whose item is held, or -1.
    private static int column = -1;
    // A column released while a screen is open, to select again when it closes, or -1.
    private static int suspendedColumn = -1;
    private static int lastTapSlot = -1;
    private static long lastTapNanos;

    private SecondBarSelection() {
    }

    public static int selectedColumn() {
        return column;
    }

    // The inventory slot whose item is drawn in a column of the hotbar or the second bar.
    public static int displaySlot(boolean secondBar, int barColumn) {
        boolean swapped = barColumn == column;
        return secondBar != swapped ? DualHotbar.SECOND_BAR_START + barColumn : barColumn;
    }

    // A full hotbar swap is starting. A held column already has its two items where the swap would put them, so the
    // swap skips it and the held item simply becomes the hotbar's. Returns that column, or -1.
    public static int handOverToSwap() {
        int held = column;
        column = -1;
        suspendedColumn = -1;
        return held;
    }

    // One scroll step along both bars as an 18-slot ring: off the right end of one bar onto the left end of the
    // other. Returns the hotbar slot vanilla should select.
    public static int scroll(Minecraft minecraft, double wheel, int currentSelected) {
        int size = Inventory.getSelectionSize();
        int position = column >= 0 ? size + column : currentSelected;
        int next = Math.floorMod(position - (int) Math.signum(wheel), 2 * size);
        if (next >= size) {
            return selectSecond(minecraft, next - size) ? next - size : currentSelected;
        }
        release(minecraft);
        return next;
    }

    // A number key was pressed for hotbar slot `slot`. Pressing it twice quickly selects that column of the second
    // bar, and returns true so vanilla's own selection is skipped; otherwise the hotbar slot is selected as usual.
    public static boolean onNumberKey(Minecraft minecraft, int slot) {
        long now = System.nanoTime();
        boolean doubleTap = slot == lastTapSlot && now - lastTapNanos < DOUBLE_TAP_NANOS;
        lastTapSlot = doubleTap ? -1 : slot;
        lastTapNanos = now;
        if (doubleTap && DualHotbarConfig.selectableSecondBar() && DualHotbarConfig.doubleTapNumberKeys()) {
            return selectSecond(minecraft, slot);
        }
        release(minecraft);
        return false;
    }

    public static boolean scrollsPastEdge() {
        return DualHotbarConfig.selectableSecondBar() && DualHotbarConfig.scrollPastEdge();
    }

    // Holds the item in second-bar column `barColumn`. Returns false when that isn't possible right now.
    public static boolean selectSecond(Minecraft minecraft, int barColumn) {
        LocalPlayer player = minecraft.player;
        if (player == null || SwapAnimation.isAnimating()) {
            return false;
        }
        if (column != barColumn) {
            if (!release(minecraft) || !swapColumn(minecraft, barColumn)) {
                return false;
            }
            column = barColumn;
        }
        player.getInventory().setSelectedSlot(barColumn);
        return true;
    }

    // Puts a held second-bar item back. Returns false if it couldn't, in which case tick() tries again.
    public static boolean release(Minecraft minecraft) {
        if (column >= 0) {
            if (!swapColumn(minecraft, column)) {
                return false;
            }
            column = -1;
        }
        suspendedColumn = -1;
        return true;
    }

    // Called at the end of every client tick.
    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null) {
            // Left the world; the swap was undone when the pause menu opened.
            column = -1;
            suspendedColumn = -1;
            return;
        }
        Screen screen = DualHotbar.screen();
        if (column >= 0) {
            boolean selectionMoved = minecraft.player.getInventory().getSelectedSlot() != column;
            if (selectionMoved || !DualHotbarConfig.selectableSecondBar()) {
                release(minecraft);
            } else if (screen instanceof PauseScreen || screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
                int held = column;
                if (release(minecraft)) {
                    suspendedColumn = held;
                }
            }
        } else if (suspendedColumn >= 0 && screen == null) {
            int held = suspendedColumn;
            suspendedColumn = -1;
            if (DualHotbarConfig.selectableSecondBar() && minecraft.player.getInventory().getSelectedSlot() == held) {
                selectSecond(minecraft, held);
            }
        }
    }

    // Swaps hotbar slot `barColumn` with the second-bar slot above it. Only possible while the player's own inventory
    // menu is the open one, which is every time except while a container (chest, furnace, ...) is open.
    private static boolean swapColumn(Minecraft minecraft, int barColumn) {
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || gameMode == null || player.isSpectator() || player.containerMenu != player.inventoryMenu) {
            return false;
        }
        Inventory inventory = player.getInventory();
        int secondSlot = DualHotbar.SECOND_BAR_START + barColumn;
        if (!inventory.getItem(barColumn).isEmpty() || !inventory.getItem(secondSlot).isEmpty()) {
            gameMode.handleContainerInput(player.inventoryMenu.containerId, secondSlot, barColumn, ContainerInput.SWAP, player);
        }
        return true;
    }
}
