package com.rebelkeithy.dualhotbar.compatability;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeVersion;

public interface ICompatabilityTools
{
	public EntityPlayerSP thePlayer();
	
	public int animationsToGo(ItemStack stack);
	
	public ItemStack getInSlot(InventoryPlayer inventory, int index);
	
	public boolean isItemStackNull(ItemStack stack);
}
