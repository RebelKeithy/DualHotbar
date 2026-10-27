package io.github.rebelkeithy.dualhotbar.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.rebelkeithy.dualhotbar.HotbarRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.contextualbar.ContextualBar;
//?} else {
/*import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Moves the info bar's foreground (the locator bar's waypoint dots) up with its background. Fabric API's INFO_BAR
// element only covers the background, and only 26.3 adds an element for the foreground, so this is used on every
// version to keep one code path.
//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public abstract class InfoBarForegroundMixin {
    //? if >=26.2 {
    @WrapOperation(
            method = "extractHotbarAndDecorations",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
            )
    )
    private void dualhotbar$shiftUp(ContextualBar bar, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        HotbarRenderer.extractShiftedUp(graphics, HotbarRenderer.decorationShift(), () -> original.call(bar, graphics, deltaTracker));
    }
    //?} else {
    /*@WrapOperation(
            method = "extractHotbarAndDecorations",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
            )
    )
    private void dualhotbar$shiftUp(ContextualBarRenderer bar, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
        HotbarRenderer.extractShiftedUp(graphics, HotbarRenderer.decorationShift(), () -> original.call(bar, graphics, deltaTracker));
    }
    *///?}
}
