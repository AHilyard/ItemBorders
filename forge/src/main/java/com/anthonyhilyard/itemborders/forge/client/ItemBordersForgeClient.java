package com.anthonyhilyard.itemborders.forge.client;

import com.anthonyhilyard.itemborders.ItemBorders;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@EventBusSubscriber(modid = ItemBorders.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public final class ItemBordersForgeClient
{
	@SubscribeEvent
	public static void onConstructMod(final FMLConstructModEvent event)
	{
		ItemBorders.init();
	}
}
