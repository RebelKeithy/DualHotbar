package com.rebelkeithy.dualhotbar;

import java.util.Map;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = DualHotbarMod.MODID, version = DualHotbarMod.VERSION, guiFactory = "com.rebelkeithy.dualhotbar.DualHotbarGuiFactory", acceptedMinecraftVersions = "[1.10,1.12)")
public class DualHotbarMod
{
    public static final String MODID = "dualhotbar";
    public static final String VERSION = "1.7.2";

	@Instance(MODID)
	public static DualHotbarMod instance;
	
	@SidedProxy(clientSide="com.rebelkeithy.dualhotbar.ProxyClient", serverSide="com.rebelkeithy.dualhotbar.ProxyCommon")
	public static ProxyCommon proxy;
	
	public static boolean installedOnServer;
	public static boolean isForgeServer = false;
	
	public static int hotbarSize = 9;
	
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	DualHotbarConfig.init(event.getSuggestedConfigurationFile());
    	
    	MinecraftForge.EVENT_BUS.register(this);
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
    	}
    	else if(DualHotbarConfig.enable)
    	{
        	System.out.println("DualHotbars installed on server. Enabling selecting slots");
    		hotbarSize = 9 * DualHotbarConfig.numHotbars;
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
		if(event.getModID().equals(MODID))
		{
			DualHotbarConfig.update();
		}
	}
	
	public static int inventorySlotOffset(int slot)
	{
		if(slot > 9)
			return 0;

		return 36;
	}
}
