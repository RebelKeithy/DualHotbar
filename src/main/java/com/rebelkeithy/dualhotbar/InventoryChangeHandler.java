package com.rebelkeithy.dualhotbar;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.network.play.client.C16PacketClientStatus;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class InventoryChangeHandler 
{
	public static KeyBinding swapkey;
	public int mousePrev = -1;
	public int slot = -1;

	public static KeyBinding selectKey;
	public boolean swapKeyDown;
    public long[] keyTimes = new long[9];
    public int lastKey = -1;
    public int clickCount = 0;
    
    public boolean[] keyWasDown = new boolean[9];
    public boolean[] changeInv = new boolean[9];

    @SubscribeEvent
    public void postTickEvent(TickEvent.ClientTickEvent event)
    {    	 
    	if(event.phase == TickEvent.Phase.START)
    	{   	
        	if(mousePrev == -1)
        		mousePrev = Mouse.getDWheel();
    		
        	
        	
    		if(Keyboard.isKeyDown(swapkey.getKeyCode()) && Math.abs(mousePrev - Mouse.getDWheel()) > 0)
    		{
    			if(swapKeyDown == false)
    			{
	    			swapKeyDown = true;
    				System.out.println(Mouse.getX() + " " + Mouse.getY());
	    			Minecraft mc = Minecraft.getMinecraft();
    				PlayerControllerMP controller = Minecraft.getMinecraft().playerController;
    				EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;

    				int window = player.inventoryContainer.windowId;
    				
    				controller.updateController();
    				
    				for(int i = 9; i < 18; i++)
    				{
    					controller.windowClick(window, i, 0, 0, player);
    					controller.windowClick(window, i+27, 0, 0, player);
    					controller.windowClick(window, i, 0, 0, player);
    				}
    				
    				slot = player.inventory.currentItem;
    			}
    		}
    		else
    		{
    			swapKeyDown = false;
    		}
    	}
    	if(event.phase == TickEvent.Phase.END)
    	{   	
    		if(slot != -1)
    		{
    			Minecraft.getMinecraft().thePlayer.inventory.currentItem = slot;
    			slot = -1;
    		}
    		
    		if(!DualHotbarConfig.enable || !DualHotbarMod.installedOnServer)
    		{
    			return;
    		}
    		
    		Minecraft mc = Minecraft.getMinecraft();
    		long time = System.currentTimeMillis();
            for (int j = 0; j < 9; ++j)
            {

            	if(Keyboard.isKeyDown(mc.gameSettings.keyBindsHotbar[j].getKeyCode()))
    			{
            		if(Keyboard.isKeyDown(selectKey.getKeyCode()))
            		{
            			Minecraft.getMinecraft().thePlayer.inventory.currentItem = j + 9;
            			continue;
            		}
            		
            		System.out.println("test");
            		if(keyWasDown[j])
            		{
            			continue;
            		}
            		
        			keyTimes[j] = time;
            		
            		if(lastKey == j && DualHotbarConfig.doubleTap && time - keyTimes[j] < 900)
            		{
            			clickCount++;
            			
            			if(clickCount > 1)
            				clickCount = 0;
            		}
            		else
            		{
            			clickCount = 0;
            		}
            		
            		if(clickCount == 1)
            		{
            			Minecraft.getMinecraft().thePlayer.inventory.currentItem = j + 9;
            		}
            		
            		lastKey = j;
            		keyWasDown[j] = true;
            		
            	}
            	else
            	{
            		keyWasDown[j] = false;
            	}
            }
    	}
    }
}
