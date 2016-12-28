package com.rebelkeithy.dualhotbar.compatability;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CompatabilityTools10 implements ICompatabilityTools
{
	
	public EntityPlayerSP thePlayer()
	{
		try {
			Field p = ReflectionHelper.findField(Minecraft.getMinecraft().getClass(), "h", "field_71439_g", "thePlayer");
			return (EntityPlayerSP) p.get(Minecraft.getMinecraft());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public int animationsToGo(ItemStack stack)
	{
		try {
			Field f = ReflectionHelper.findField(stack.getClass(), "c", "field_77992_b", "animationsToGo");
			return (Integer) f.get(stack);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public ItemStack getInSlot(InventoryPlayer inventory, int index)
	{
		try
		{
			Field f = ReflectionHelper.findField(inventory.getClass(), "a", "field_70462_a", "mainInventory");
			return ((ItemStack[])f.get(inventory))[index];
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean isItemStackNull(ItemStack stack)
	{
		return stack == null;
	}
}
