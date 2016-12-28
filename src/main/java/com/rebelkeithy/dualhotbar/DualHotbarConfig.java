package com.rebelkeithy.dualhotbar;

import java.io.File;

import net.minecraftforge.common.config.Configuration;


public class DualHotbarConfig 
{
	public static Configuration config;
	
	public static boolean enable;
	public static boolean twoLayerRendering;
	public static boolean doubleTap;
	public static boolean keyCombo;
	public static int doubleTapTime;
	
	public static int numHotbars = 4;
	
	public static void init(File file)
	{
		config = new Configuration(file);
		
		config.load();
		update();
	}

	public static void update() 
	{
		config.renameProperty("Config", "Render Two Layers", "Long Hotbar");
		enable = config.getBoolean("Enable", "Config", true, "Enable DualHotbar");
		twoLayerRendering = config.getBoolean("Stacked Hotbar", "Config", true, "If disable for one long hotbar");
		doubleTap = config.getBoolean("Enable Double Tap", "Config", true, "Double tap the inventory key to select the upper layer item");
		keyCombo = config.getBoolean("Enable Key Combo", "Config", false, "Use key combo to select the upper layer item");
		doubleTapTime = config.getInt("Double Tap Time", "Config", 900, 0, 2000, "Time (in milliseconds) for double tapping");
		numHotbars = config.getInt("Number of Hotbars", "Config", 2, 1, 4, "How many hotbar rows (9 slots each)");
		
		if(numHotbars == 3)
		{
			twoLayerRendering = true;
		}
		
		if(numHotbars < 1 || numHotbars > 4)
		{
			numHotbars = 1;
		}
		
		if(enable)
		{
			DualHotbarMod.hotbarSize = 18;
		}
		else
		{
			DualHotbarMod.hotbarSize = 9;
		}
		
		config.save();
	}
}
