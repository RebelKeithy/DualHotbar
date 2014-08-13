package com.rebelkeithy.dualhotbar;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;

public class ProxyClient extends ProxyCommon
{
	@Override
	public void init()
	{
		RenderHandler renderHandler = new RenderHandler();
		InventoryChangeHandler inventoryChangeHandler = new InventoryChangeHandler();
		
		inventoryChangeHandler.selectKey = new KeyBinding("Hold For Second 9", Keyboard.KEY_LCONTROL, "key.categories.inventory");
		ClientRegistry.registerKeyBinding(inventoryChangeHandler.selectKey);
		
		inventoryChangeHandler.swapkey = new KeyBinding("Hold+Wheel to Swap Bars", Keyboard.KEY_LCONTROL, "key.categories.inventory");
		ClientRegistry.registerKeyBinding(inventoryChangeHandler.swapkey);
		
		//FMLCommonHandler.instance().bus().register(renderHandler);
		MinecraftForge.EVENT_BUS.register(renderHandler);
		FMLCommonHandler.instance().bus().register(inventoryChangeHandler);
	}
}
