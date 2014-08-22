package com.rebelkeithy.dualhotbar;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = DualHotbarMod.MODID, version = DualHotbarMod.VERSION, guiFactory = "com.rebelkeithy.dualhotbar.DualHotbarGuiFactory")
public class DualHotbarMod
{
    public static final String MODID = "dualhotbar";
    public static final String VERSION = "1.5.1";

	@Instance(MODID)
	public static DualHotbarMod instance;
	
	@SidedProxy(clientSide="com.rebelkeithy.dualhotbar.ProxyClient", serverSide="com.rebelkeithy.dualhotbar.ProxyCommon")
	public static ProxyCommon proxy;
	
	public static boolean installedOnServer;
	public static boolean isForgeServer = false;
	
	public static int hotbarSize = 9;
	public static int value = 36;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	DualHotbarConfig.init(event.getSuggestedConfigurationFile());
    	
    	FMLCommonHandler.instance().bus().register(this);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	proxy.init();
    }
    
    // By this point checkRemote would have been called, so we can set the hotbar size to the correct values depending on if the server has this mod
    @SubscribeEvent
    public void onConnectedToServerEvent(ClientConnectedToServerEvent event) 
    {
    	if(!installedOnServer)
    	{
        	System.out.println("DualHotbars not installed on server. Disabling selecting slots");
    		hotbarSize = 9;
    		value = 36;
    	}
    	else if(DualHotbarConfig.enable)
    	{
        	System.out.println("DualHotbars installed on server. Enabling selecting slots");
    		hotbarSize = 9 * DualHotbarConfig.numHotbars;
    		value = 45 - 9 * DualHotbarConfig.numHotbars;
    	}
    }
    
    // Assume the server doesn't have this mod installed, so installedOnServer is reset to false when leaving a server
    @SubscribeEvent
    public void onClientDisconnectionFromServerEvent(ClientDisconnectionFromServerEvent event) 
    {
    	installedOnServer = false;
    }
	
    // This is called before the ClientConnectedToServerEvent event, but this is only called on forge servers
	@NetworkCheckHandler
	public boolean checkRemote(Map<String,String> mods, Side remoteSide)
	{
		System.out.println("checking remote");
		
		for(String s : mods.keySet())
		{
			System.out.println(s + " " + mods.get(s));
			if(s.equals(MODID))
			{
				installedOnServer = true;
				break;
			}
		}
		return true;
	}
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if(event.modID.equals(MODID))
		{
			DualHotbarConfig.update();
		}
	}
}
