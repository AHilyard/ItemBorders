package com.anthonyhilyard.itemborders.neoforge.client;

import com.anthonyhilyard.itemborders.ItemBorders;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = ItemBorders.MODID, dist = Dist.CLIENT)
public final class ItemBordersNeoForgeClient
{
	public ItemBordersNeoForgeClient()
	{
		ItemBorders.init();
	}
}

