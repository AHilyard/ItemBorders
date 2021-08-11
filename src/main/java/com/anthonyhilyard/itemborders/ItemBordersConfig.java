package com.anthonyhilyard.itemborders;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public class ItemBordersConfig
{
	public static final ForgeConfigSpec SPEC;
	public static final ItemBordersConfig INSTANCE;
	static
	{
		Pair<ItemBordersConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ItemBordersConfig::new);
		SPEC = specPair.getRight();
		INSTANCE = specPair.getLeft();
	}

	public final BooleanValue hotBar;
	public final BooleanValue showForCommon;

	public ItemBordersConfig(ForgeConfigSpec.Builder build)
	{
		build.comment("Client Configuration").push("client").push("options");

		hotBar = build.comment("If the hotbar should display item borders.").define("hotbar", true);
		showForCommon = build.comment("If item borders should show for common items.").define("show_for_common", false);
		
		build.pop().pop();
	}

	@SubscribeEvent
	public static void onLoad(ModConfig.Loading e)
	{
		if (e.getConfig().getModId().equals(Loader.MODID))
		{
			Loader.LOGGER.info("Advancement Plaques config reloaded.");
		}
	}

}