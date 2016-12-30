package com.rebelkeithy.dualhotbar;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ProxyClient extends ProxyCommon
{
	@Override
	public void init()
	{
		RenderHandler renderHandler = new RenderHandler();
		InventoryChangeHandler inventoryChangeHandler = new InventoryChangeHandler();
		
		InventoryChangeHandler.selectKey = new KeyBinding("Hold For Second 9", Keyboard.KEY_LCONTROL, "key.categories.inventory");
		ClientRegistry.registerKeyBinding(InventoryChangeHandler.selectKey);
		
		InventoryChangeHandler.swapkey = new KeyBinding("Hold+Wheel to Swap Bars", Keyboard.KEY_LCONTROL, "key.categories.inventory");
		ClientRegistry.registerKeyBinding(InventoryChangeHandler.swapkey);
		
		MinecraftForge.EVENT_BUS.register(renderHandler);
		MinecraftForge.EVENT_BUS.register(inventoryChangeHandler);
	}
}
