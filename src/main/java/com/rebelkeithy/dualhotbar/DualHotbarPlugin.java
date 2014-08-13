package com.rebelkeithy.dualhotbar;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class DualHotbarPlugin implements IFMLLoadingPlugin
{

	@Override
	public String[] getASMTransformerClass() 
	{
		return new String[]{ DualHotbarTransformer.class.getName() };
	}

	@Override
	public String getModContainerClass() 
	{
		return null;
	}
	
	@Override
	public String getSetupClass() 
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) 
	{
		
	}

	@Override
	public String getAccessTransformerClass() 
	{
		return null;
	}

}
