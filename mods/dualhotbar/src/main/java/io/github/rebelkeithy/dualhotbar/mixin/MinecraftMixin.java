package io.github.rebelkeithy.dualhotbar.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.rebelkeithy.dualhotbar.SecondBarSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Number keys: vanilla selects hotbar slot N; with the Second Bar Modifier key held, or on a quick second press of the
// same key, second-bar slot N is selected instead.
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @WrapOperation(
            method = "handleKeybinds",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V")
    )
    private void dualhotbar$doubleTapNumberKey(Inventory inventory, int slot, Operation<Void> original) {
        if (!SecondBarSelection.onNumberKey((Minecraft) (Object) this, slot)) {
            original.call(inventory, slot);
        }
    }
}
