package com.rebelkeithy.dualhotbar.compatability;

public class Compatability
{	
	public static boolean isObfuscated = true; //TODO: Change this to 'true' before publishing
	private static ICompatabilityTools version;
	
	public static void init()
	{
		version = new CompatabilityTools11();
		/*
		System.out.println("MC VERSION: " + Minecraft.getMinecraft().getVersion());
		if(Minecraft.getMinecraft().getVersion().contains("12.18"))
		{
			System.out.println("*** Choosing Version 1.10 Compatability ***");
			version = new CompatabilityTools10();
			return;
		}
		else
		{
			System.out.println("*** Choosing Version 1.11 Compatability ***");
			version = new CompatabilityTools11();
			return;
		}
		*/
	}
	
	public static ICompatabilityTools instance()
	{
		if(version == null)
			init();
		
		return version;
	}
}
