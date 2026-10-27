package io.github.rebelkeithy.dualhotbar.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.rebelkeithy.dualhotbar.DualHotbarConfig;
import io.github.rebelkeithy.dualhotbar.DualHotbarKeys;
import io.github.rebelkeithy.dualhotbar.HotbarSwapper;
import io.github.rebelkeithy.dualhotbar.SecondBarSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Scrolling while holding the swap modifier key (Shift by default) swaps the hotbars instead of moving the selection.
// On macOS, Shift turns a mouse wheel into horizontal scrolling, which vanilla already folds into the same wheel value,
// so both directions arrive here. As in vanilla, a negative wheel value is scrolling down (the direction that selects
// the next slot). Scroll steps that arrive while a swap is still animating are dropped (HotbarSwapper.swap), so a
// fast stream of steps can't stack up swaps. Without the modifier, scrolling can also move the selection past the end
// of one bar onto the other (SecondBarSelection).
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @WrapOperation(
            method = "onScroll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ScrollWheelHandler;getNextScrollWheelSelection(DII)I")
    )
    private int dualhotbar$swapOnShiftScroll(double wheel, int currentSelected, int limit, Operation<Integer> original) {
        Minecraft minecraft = Minecraft.getInstance();
        if (DualHotbarConfig.scrollToSwap() && DualHotbarKeys.scrollModifierDown()) {
            HotbarSwapper.swap(minecraft, wheel < 0);
            return currentSelected;
        }
        if (SecondBarSelection.scrollsPastEdge()) {
            return SecondBarSelection.scroll(minecraft, wheel, currentSelected);
        }
        SecondBarSelection.release(minecraft);
        return original.call(wheel, currentSelected, limit);
    }
}
