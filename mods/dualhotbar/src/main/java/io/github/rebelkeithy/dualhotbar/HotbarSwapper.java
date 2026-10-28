package io.github.rebelkeithy.dualhotbar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;

public final class HotbarSwapper {
    private HotbarSwapper() {
    }

    // Swaps each hotbar slot with the second-hotbar slot in the same column, using the "swap with hotbar slot N" click
    // that pressing a number key over an inventory slot sends. The server stays in charge of the inventory, so this
    // works on servers without the mod, and the client predicts the result so the new items show at once.
    // wheelDown picks which way the swap animation turns. Returns false when no swap is possible right now, including
    // while the previous swap is still animating, and always in Long Hotbar, which is one 18-slot bar with nothing to
    // swap.
    public static boolean swap(Minecraft minecraft, boolean wheelDown) {
        LocalPlayer player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (DualHotbarConfig.longHotbarInUse() || SwapAnimation.isAnimating() || player == null || gameMode == null || player.isSpectator()
                || player.containerMenu != player.inventoryMenu) {
            return false;
        }

        Inventory inventory = player.getInventory();
        int heldSecondColumn = SecondBarSelection.handOverToSwap();
        for (int column = 0; column < Inventory.getSelectionSize(); column++) {
            int slot = DualHotbar.SECOND_BAR_START + column;
            if (column != heldSecondColumn && (!inventory.getItem(column).isEmpty() || !inventory.getItem(slot).isEmpty())) {
                gameMode.handleContainerInput(player.inventoryMenu.containerId, slot, column, ContainerInput.SWAP, player);
            }
        }
        SwapAnimation.start(wheelDown);
        return true;
    }
}
