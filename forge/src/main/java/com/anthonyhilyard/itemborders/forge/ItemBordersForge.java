package com.anthonyhilyard.itemborders.forge;

import com.anthonyhilyard.itemborders.ItemBorders;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(ItemBorders.MODID)
public final class ItemBordersForge
{
	public ItemBordersForge()
	{
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));
	}
}
