package com.rebelkeithy.dualhotbar;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class DualHotbarTransformer implements IClassTransformer
{

	@Override
	public byte[] transform(String className, String newClassName, byte[] data) 
	{		
		boolean isObfuscated = !className.equals(newClassName);
		
		if (newClassName.equals("net.minecraft.entity.player.InventoryPlayer")) 
		{
			System.out.println("********* INSIDE InventoryPlayer TRANSFORMER ABOUT TO PATCH: " + className);

			if(!isObfuscated)
			{
				data = patchBipush(className, "getCurrentItem", "()Lnet/minecraft/item/ItemStack;", data);
				data = patchBipush(className, "getHotbarSize", "()I", data);
				return patchBipush(className, "changeCurrentItem", "(I)V", data);
			}
			else
			{
				boolean is1710 = containsMethod("h", "()add;", data);
				
				if(is1710)
				{
					data = patchBipush(className, "h", "()Ladd;", data);
				}
				else
				{
					data = patchBipush(className, "h", "()Labp;", data);
				}
				
				data = patchBipush(className, "i", "()I", data);
				return patchBipush(className, "c", "(I)V", data);
			}
		}
		
		if(newClassName.equals("net.minecraft.network.NetHandlerPlayServer"))
		{
			System.out.println("********* INSIDE NetHandlerPlayServer TRANSFORMER ABOUT TO PATCH: " + className);

			if(!isObfuscated)
			{
				return patchBipush2(className, "processCreativeInventoryAction", "(Lnet/minecraft/network/play/client/C10PacketCreativeInventoryAction;)V", data);
			}
			else
			{
				boolean is1710 = containsMethod("a", "(Ljm;)V", data);
				
				if(is1710)
				{
					return patchBipush2(className, "a", "(Ljm;)V", data);
				}
				else
				{
					return patchBipush2(className, "a", "(Lja;)V", data);
				}
			}
		}
		
		if(className.equals("net.minecraftforge.common.ForgeHooks"))
		{
			System.out.println("********* INSIDE ForgeHooks TRANSFORMER ABOUT TO PATCH: " + className);
			return patchBipush(className, "onPickBlock", null, data);
		}
		
		if(className.equals("net.minecraftforge.client.GuiIngameForge"))
		{
			System.out.println("********* INSIDE GuiIngameForge TRANSFORMER ABOUT TO PATCH: " + className);
			return patchShift(className, "renderToolHightlight", null, data);
		}
		
		return data;
	}

	private byte[] patchBipush(String className, String methodName, String methodDesc, byte[] data) 
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(data);
		classReader.accept(classNode, 0);
		
		Iterator<MethodNode> iter = classNode.methods.iterator();
		while(iter.hasNext())
		{
			MethodNode methodNode = iter.next();
			
			if(methodNode.name.equals(methodName) && (methodDesc == null || methodNode.desc.equals(methodDesc)))
			{
				System.out.println("In " + methodName);

				Iterator<AbstractInsnNode> insnIter = methodNode.instructions.iterator();
				AbstractInsnNode insnNode = insnIter.next();
				
				while(insnIter.hasNext())
				{
					if(insnNode.getOpcode() == Opcodes.BIPUSH)
					{
						System.out.println("found instruction to replace");

						AbstractInsnNode newInstruction = new IntInsnNode(Opcodes.BIPUSH, 18);

						newInstruction = new FieldInsnNode(Opcodes.GETSTATIC, "com/rebelkeithy/dualhotbar/DualHotbarMod", "hotbarSize", "I");
						
						methodNode.instructions.insert(insnNode, newInstruction);
						methodNode.instructions.remove(insnNode);
					}
					insnNode = insnIter.next();
				}
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private byte[] patchBipush2(String className, String methodName, String methodDesc, byte[] data) 
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(data);
		classReader.accept(classNode, 0);
		
		Iterator<MethodNode> iter = classNode.methods.iterator();
		while(iter.hasNext())
		{
			MethodNode methodNode = iter.next();
			
			if(methodNode.name.equals(methodName) && methodNode.desc.equals(methodDesc))
			{
				System.out.println("In " + methodName);

				Iterator<AbstractInsnNode> insnIter = methodNode.instructions.iterator();
				
				System.out.println("Number of instructions " + methodNode.instructions.size());
				
				while(insnIter.hasNext())
				{
					AbstractInsnNode insnNode = insnIter.next();

					System.out.println(insnNode);
					if(insnNode instanceof IntInsnNode)
					{
						System.out.println(((IntInsnNode)insnNode).operand);
					}
					
					if(insnNode.getOpcode() == Opcodes.BIPUSH)
					{
						System.out.println("found instruction to replace");

						AbstractInsnNode newInstruction = new IntInsnNode(Opcodes.BIPUSH, 27);
						
						newInstruction = new FieldInsnNode(Opcodes.GETSTATIC, "com/rebelkeithy/dualhotbar/DualHotbarMod", "value", "I");
						
						methodNode.instructions.insert(insnNode, newInstruction);
						methodNode.instructions.remove(insnNode);
						break;
					}
				}
			}
		}
		

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private byte[] patchShift(String className, String methodName, String methodDesc, byte[] data) 
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(data);
		classReader.accept(classNode, 0);
		
		Iterator<MethodNode> iter = classNode.methods.iterator();
		while(iter.hasNext())
		{
			MethodNode methodNode = iter.next();
			
			if(methodNode.name.equals(methodName) && (methodNode.desc.equals(methodDesc) || methodDesc == null))
			{
				System.out.println("In " + methodName);

				Iterator<AbstractInsnNode> insnIter = methodNode.instructions.iterator();
				

				AbstractInsnNode insnNode = insnIter.next();

				AbstractInsnNode newInstruction = new MethodInsnNode(Opcodes.INVOKESTATIC, "com/rebelkeithy/dualhotbar/RenderHandler", "shiftUp", "()V");
				methodNode.instructions.insertBefore(insnNode, newInstruction);
				
				while(insnNode.getOpcode() != Opcodes.RETURN)
				{
					insnNode = insnIter.next();
				}

				newInstruction = new MethodInsnNode(Opcodes.INVOKESTATIC, "com/rebelkeithy/dualhotbar/RenderHandler", "shiftDown", "()V");
				methodNode.instructions.insertBefore(insnNode, newInstruction);
			}
		}
		

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private boolean containsMethod(String methodName, String methodDesc, byte[] data) 
	{
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(data);
		classReader.accept(classNode, 0);
		
		Iterator<MethodNode> iter = classNode.methods.iterator();
		while(iter.hasNext())
		{
			MethodNode methodNode = iter.next();
			
			if(methodNode.name.equals(methodName) && methodNode.desc.equals(methodDesc))
			{
				return true;
			}
		}
		
		return false;
	}
}
