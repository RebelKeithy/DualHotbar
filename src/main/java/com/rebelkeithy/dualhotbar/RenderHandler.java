package com.rebelkeithy.dualhotbar;

import java.lang.reflect.Constructor;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.rebelkeithy.dualhotbar.compatability.Compatability;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderHandler 
{
	public static int switchTicks = 0;
	//public static int maxSwitchTicks = 18;
	
    private static final ResourceLocation WIDGITS = new ResourceLocation("textures/gui/widgets.png");
    
    private boolean recievedPost = true;

	private static Constructor<ScaledResolution> scaledResolution172Constructor = null;
	static
	{
		try
		{
			scaledResolution172Constructor = ScaledResolution.class.getConstructor(GameSettings.class, int.class, int.class);
		}
		catch(Exception e) {}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderHotbar(RenderGameOverlayEvent.Pre event)
    {
		if(!DualHotbarConfig.enable)
		{
			return;
		}
		
    	if(event.getType() == ElementType.HOTBAR)
    	{
	    	Minecraft mc = Minecraft.getMinecraft();
	    	float partialTicks = event.getPartialTicks();
	    	ScaledResolution res = event.getResolution();
			
	        int width = res.getScaledWidth();
	        int height = res.getScaledHeight();
	        
	        mc.mcProfiler.startSection("actionBar");
	        
	        int offset = 20;
	
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        mc.renderEngine.bindTexture(WIDGITS);

	        GL11.glPushMatrix();
	        if(!DualHotbarConfig.twoLayerRendering)
	        {
	        	if(DualHotbarConfig.numHotbars == 4)
	        		GL11.glTranslatef(0, -41, 0);
	        	else
	        		GL11.glTranslatef(0, -21, 0);
	        }
	        
	        // Draw the offhand slot
            EntityPlayer entityplayer = (EntityPlayer)mc.getRenderViewEntity();
            ItemStack itemstack = entityplayer.getHeldItemOffhand();
            EnumHandSide enumhandside = entityplayer.getPrimaryHand().opposite();
            if (!itemstack.isEmpty())
            {
                if (enumhandside == EnumHandSide.LEFT)
                {
                	mc.ingameGUI.drawTexturedModalRect(width / 2 - 91 - 29, res.getScaledHeight() - 23, 24, 22, 29, 24);
                }
                else
                {
                	mc.ingameGUI.drawTexturedModalRect(width / 2 + 91, res.getScaledHeight() - 23, 53, 22, 29, 24);
                }
            }
            
            GL11.glPopMatrix();
	        
            // Draw the hotbar slots
	        InventoryPlayer inv = Compatability.instance().thePlayer().inventory;
	        if(DualHotbarConfig.twoLayerRendering)
	        {
	        	mc.ingameGUI.drawTexturedModalRect(width / 2 - 91, height - 22, 0, 0, 182, 22);
	        	
	        	if(!DualHotbarMod.installedOnServer)
	        		GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
	        	
	        	for(int i = 1; i < DualHotbarConfig.numHotbars; i++)
	        		mc.ingameGUI.drawTexturedModalRect(width / 2 - 91, height - 22 * i - offset + (i - 1)*2, 0, 0, 182, 21);
	        	
	        	if(!DualHotbarMod.installedOnServer)
	        		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1f);
	        	
	        	// Draw selection square
	        	mc.ingameGUI.drawTexturedModalRect(width / 2 - 91 - 1 + (inv.currentItem%9) * 20, height - 22 - 1 - ((inv.currentItem/9) * offset), 0, 22, 24, 22);
	        	mc.ingameGUI.drawTexturedModalRect(width / 2 - 91 - 1 + (inv.currentItem%9) * 20, height - 1 - ((inv.currentItem/9) * offset), 0, 22, 24, 1);
	        }
	        else
	        {
	        	mc.ingameGUI.drawTexturedModalRect(width / 2 - 91 - 90, height - 22, 0, 0, 182, 22);
	        	mc.ingameGUI.drawTexturedModalRect(width / 2 - 91 + 91, height - 22, 1, 0, 181, 22);
	        	mc.ingameGUI.drawTexturedModalRect(width / 2 - 91 + 91 - 1, height - 22, 20, 0, 3, 22);
	        	if(DualHotbarConfig.numHotbars == 4)
	        	{
		        	mc.ingameGUI.drawTexturedModalRect(width / 2 - 91 - 90, height - 22 - offset, 0, 0, 182, 21);
		        	mc.ingameGUI.drawTexturedModalRect(width / 2 - 91 + 91, height - 22 - offset, 1, 0, 181, 21);
		        	mc.ingameGUI.drawTexturedModalRect(width / 2 - 91 + 91 - 1, height - 22 - offset, 20, 0, 3, 21);
	        	}
	        	mc.ingameGUI.drawTexturedModalRect(width / 2 - 91 - 1 + (inv.currentItem%18) * 20 - 90, height - 22 - 1 - ((inv.currentItem/18) * offset), 0, 22, 24, 22);
	        	mc.ingameGUI.drawTexturedModalRect(width / 2 - 91 - 1 + (inv.currentItem%18) * 20 - 90, height - 1 - ((inv.currentItem/18) * offset), 0, 22, 24, 1);
	        }

            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderHelper.enableGUIStandardItemLighting();

	        GL11.glPushMatrix();
	        if(!DualHotbarConfig.twoLayerRendering)
	        {
	        	if(DualHotbarConfig.numHotbars == 4)
	        		GL11.glTranslatef(0, -41, 0);
	        	else
	        		GL11.glTranslatef(0, -21, 0);
	        }

            if (!itemstack.isEmpty())
            {
                int l1 = res.getScaledHeight() - 16 - 3;

                if (enumhandside == EnumHandSide.LEFT)
                {
                    this.renderHotbarItem(width / 2 - 91 - 26, l1, partialTicks, entityplayer, itemstack);
                }
                else
                {
                    this.renderHotbarItem(width / 2 + 91 + 10, l1, partialTicks, entityplayer, itemstack);
                }
            }
            
            GL11.glPopMatrix();

	        // Draw the hotbar items
	        for (int i = 0; i < 9 * DualHotbarConfig.numHotbars; ++i)
	        {
	        	if(DualHotbarConfig.twoLayerRendering)
	        	{
	        		int x = width / 2 - 90 + (i%9) * 20 + 2;
	            	int z = height - 16 - 3 - ((i/9) * offset);
		        	if(!DualHotbarMod.installedOnServer)
		        		GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);

		        	GL11.glPushMatrix();
		        	if(RenderHandler.switchTicks != 0)
		        	{
		        		float animationOffset = RenderHandler.switchTicks * 2;
		        		
		        		if(RenderHandler.switchTicks < 0)
		        			animationOffset += 2;
		        		if(RenderHandler.switchTicks > 0)
		        			animationOffset -= 2;
		        		
		        		if(RenderHandler.switchTicks < 0 && i/9 == DualHotbarConfig.numHotbars - 1 && RenderHandler.switchTicks < -6)
		        			animationOffset += 20 * DualHotbarConfig.numHotbars;
		        		
		        		if(RenderHandler.switchTicks > 0 && i/9 == 0 && RenderHandler.switchTicks > 6)
		        			animationOffset -= 20 * DualHotbarConfig.numHotbars;
		        		
		        		GL11.glTranslatef(0, animationOffset, 0);
		        	}
		        	
	                renderHotbarItem(x, z, partialTicks, entityplayer, Compatability.instance().getInSlot(entityplayer.inventory, i));
	                
		        	GL11.glPopMatrix();
	                
		        	if(!DualHotbarMod.installedOnServer)
		        		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1f);
	        	}
	        	else
	        	{
	        		int x = width / 2 - 90 + (i%18) * 20 + 2 - 90;
	            	int z = height - 16 - 3 - ((i/18) * offset);

		        	GL11.glPushMatrix();
		        	if(RenderHandler.switchTicks != 0)
		        	{
		        		float animationOffset = RenderHandler.switchTicks * 2;
		        		
		        		if(RenderHandler.switchTicks < 0)
		        			animationOffset += 2;
		        		if(RenderHandler.switchTicks > 0)
		        			animationOffset -= 2;
	        		
		        		if(RenderHandler.switchTicks < 0 && (i/9 == 2 || i/9 == 3) && RenderHandler.switchTicks < -6)
		        			animationOffset += 20 * DualHotbarConfig.numHotbars/2;
		        		
		        		if(RenderHandler.switchTicks > 0 && (i/9 == 0 || i/9 == 1) && RenderHandler.switchTicks > 6)
		        			animationOffset -= 20 * DualHotbarConfig.numHotbars/2;
		        		
		        		GL11.glTranslatef(0, animationOffset, 0);
		        	}
		        	
	                renderHotbarItem(x, z, partialTicks, entityplayer, Compatability.instance().getInSlot(entityplayer.inventory, i));

		        	GL11.glPopMatrix();
	        	}
	        }
	
	        RenderHelper.disableStandardItemLighting();
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	        mc.mcProfiler.endSection();
	        
	        if(RenderHandler.switchTicks > 0)
	        	RenderHandler.switchTicks--;
	        if(RenderHandler.switchTicks < 0)
	        	RenderHandler.switchTicks++;
	        
	        
	        // Stop minecraft from drawing the hotbar itself
	        event.setCanceled(true);
    	}
    }

	@SubscribeEvent(priority = EventPriority.HIGHEST)
    public void shiftRendererUp(RenderGameOverlayEvent.Pre event)
    {
		if(!DualHotbarConfig.enable || (!DualHotbarConfig.twoLayerRendering && DualHotbarConfig.numHotbars != 4))
		{
			return;
		}
		
		if(event.getType() == ElementType.CHAT || event.getType() == ElementType.ARMOR || event.getType() == ElementType.EXPERIENCE || event.getType() == ElementType.FOOD || event.getType() == ElementType.HEALTH || event.getType() == ElementType.HEALTHMOUNT || event.getType() == ElementType.JUMPBAR || event.getType() == ElementType.AIR/* || event.type == ElementType.TEXT*/)
    	{
    		// In some cases the post render event is not received (when the pre event is cancelled by another mod), in the case, go ahead an pop the matrix before continuing
    		if(recievedPost == false)
    		{
        		GL11.glPopMatrix();
    		}
    		
    		recievedPost = false;
    		GL11.glPushMatrix();
    		
    		if(DualHotbarConfig.twoLayerRendering)
    			GL11.glTranslatef(0, -20 * (DualHotbarConfig.numHotbars - 1), 0);
    		else
    			GL11.glTranslatef(0, -20 * (DualHotbarConfig.numHotbars/2 - 1), 0);
    	}
    }

	@SubscribeEvent(priority = EventPriority.LOWEST)
    public void shiftRendererDown(RenderGameOverlayEvent.Post event)
    {
		if(!DualHotbarConfig.enable || (!DualHotbarConfig.twoLayerRendering && DualHotbarConfig.numHotbars != 4))
		{
			return;
		}
		
    	if(event.getType() == ElementType.CHAT ||event.getType() == ElementType.ARMOR || event.getType() == ElementType.EXPERIENCE || event.getType() == ElementType.FOOD || event.getType() == ElementType.HEALTH || event.getType() == ElementType.HEALTHMOUNT || event.getType() == ElementType.JUMPBAR || event.getType() == ElementType.AIR/* || event.type == ElementType.TEXT*/)
    	{
    		recievedPost = true;
    		GL11.glPopMatrix();
    	}
    }
	
	// This is used by the asm transformer
	public static void shiftUp()
	{
		if(!DualHotbarConfig.enable || (!DualHotbarConfig.twoLayerRendering && DualHotbarConfig.numHotbars != 4))
		{
			return;
		}
		
		GL11.glPushMatrix();
		if(DualHotbarConfig.twoLayerRendering)
			GL11.glTranslatef(0, -20 * (DualHotbarConfig.numHotbars - 1), 0);
		else
			GL11.glTranslatef(0, -20 * (DualHotbarConfig.numHotbars/2 - 1), 0);
	}

	// This is used by the asm transformer
	public static void shiftDown()
	{
		if(!DualHotbarConfig.enable || (!DualHotbarConfig.twoLayerRendering && DualHotbarConfig.numHotbars != 4))
		{
			return;
		}
		
		GL11.glPopMatrix();
	}

	// Copied from GuiIngame.renderHotbarItem
    protected void renderHotbarItem(int p_184044_1_, int p_184044_2_, float p_184044_3_, EntityPlayer player, ItemStack stack)
    {
		if(!DualHotbarConfig.enable)
		{
			return;
		}
		
    	Minecraft mc = Minecraft.getMinecraft();
    	RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();

        if (!Compatability.instance().isItemStackNull(stack))
        {
            float f = Compatability.instance().animationsToGo(stack) - p_184044_3_;
            f = Math.max(f, Math.abs(RenderHandler.switchTicks/4f));

            if (f > 0.0F)
            {
                GlStateManager.pushMatrix();
                float f1 = 1.0F + f / 5.0F;
                GlStateManager.translate((float)(p_184044_1_ + 8), (float)(p_184044_2_ + 12), 0.0F);
                GlStateManager.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                GlStateManager.translate((float)(-(p_184044_1_ + 8)), (float)(-(p_184044_2_ + 12)), 0.0F);
            }
            

            itemRenderer.renderItemAndEffectIntoGUI(player, stack, p_184044_1_, p_184044_2_);

            if (f > 0.0F)
            {
                GlStateManager.popMatrix();
            }

            itemRenderer.renderItemOverlays(mc.fontRendererObj, stack, p_184044_1_, p_184044_2_);
        }
    }
}
