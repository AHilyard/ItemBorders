package com.anthonyhilyard.itemborders.neoforge;

import com.anthonyhilyard.itemborders.ItemBorders;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(ItemBorders.MODID)
public final class ItemBordersNeoForge
{
	public ItemBordersNeoForge(ModContainer container, IEventBus modBus)
	{
		// Run our common setup.
		ItemBorders.init();
	}
}

