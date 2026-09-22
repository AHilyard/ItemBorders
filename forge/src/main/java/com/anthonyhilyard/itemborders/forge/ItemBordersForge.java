package com.anthonyhilyard.itemborders.forge;

import com.anthonyhilyard.itemborders.ItemBorders;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ItemBorders.MODID)
public final class ItemBordersForge
{
	public ItemBordersForge(FMLJavaModLoadingContext context)
	{
		context.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));
	}
}
