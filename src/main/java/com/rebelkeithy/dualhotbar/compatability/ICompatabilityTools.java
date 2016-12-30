package com.rebelkeithy.dualhotbar.compatability;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public interface ICompatabilityTools
{
	public EntityPlayerSP thePlayer();
	
	public int animationsToGo(ItemStack stack);
	
	public ItemStack getInSlot(InventoryPlayer inventory, int index);
	
	public boolean isItemStackNull(ItemStack stack);
}
