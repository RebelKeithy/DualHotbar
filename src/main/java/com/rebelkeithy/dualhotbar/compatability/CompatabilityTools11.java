package com.rebelkeithy.dualhotbar.compatability;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CompatabilityTools11 implements ICompatabilityTools
{
	
	public EntityPlayerSP thePlayer()
	{
		// Not really neccessary, 1.10.2 and 1.11 both use field_71439_g
		try {
			Field p = ReflectionHelper.findField(Minecraft.getMinecraft().getClass(), "h", "field_71439_g", "player", "thePlayer");
			return (EntityPlayerSP) p.get(Minecraft.getMinecraft());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public int animationsToGo(ItemStack stack)
	{
		// Try 1.11 Method
		try {
			Method m = ReflectionHelper.findMethod(ItemStack.class, stack, new String[] {"D", "func_190921_D", "getAnimationsToGo"});
			return (Integer) m.invoke(stack);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
		}

		// Try 1.10.2 Method
		try {
			Field f = ReflectionHelper.findField(stack.getClass(), "c", "field_77992_b", "animationsToGo");
			return (Integer) f.get(stack);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
		}
		
		return -1;
	}
	
	public ItemStack getInSlot(InventoryPlayer inventory, int index)
	{

		// Try 1.10.2 Method
		try
		{
			Field f = ReflectionHelper.findField(inventory.getClass(), "a", "field_70462_a", "mainInventory");
			return ((ItemStack[])f.get(inventory))[index];
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
		}
		
		// Try 1.11 Method
		try {
			Method m = inventory.mainInventory.getClass().getMethod("get", int.class);
			return (ItemStack) m.invoke(inventory.mainInventory, index);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
		}
		
		return null;
	}
	
	public boolean isItemStackNull(ItemStack stack)
	{
		// Try 1.11 Method
		try {
			Method m = ReflectionHelper.findMethod(ItemStack.class, stack, new String[] {"b", "func_190926_b", "isEmpty"});
			return (Boolean) m.invoke(stack);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (Exception e) {
			
		}

		// Try 1.10.2 Method
		return stack == null;
	}
}
