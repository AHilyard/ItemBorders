package com.anthonyhilyard.itemborders.fabric;

import com.anthonyhilyard.itemborders.ItemBorders;

import net.fabricmc.api.ModInitializer;

public final class ItemBordersFabric implements ModInitializer
{
	@Override
	public void onInitialize()
	{
		ItemBorders.init();
	}
}
