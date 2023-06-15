package com.anthonyhilyard.itemborders.compat;

import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.anthonyhilyard.legendarytooltips.LegendaryTooltipsConfig;

import net.minecraft.item.ItemStack;

public class LegendaryTooltipsHandler
{
	public static Pair<Supplier<Integer>, Supplier<Integer>> getBorderColors(ItemStack item)
	{
		int frameLevel = LegendaryTooltipsConfig.INSTANCE.getFrameLevelForItem(item);

		if (frameLevel < 0)
		{
			return null;
		}
		int startColor = LegendaryTooltipsConfig.INSTANCE.getCustomBorderStartColor(frameLevel);
		int endColor = LegendaryTooltipsConfig.INSTANCE.getCustomBorderEndColor(frameLevel);

		return Pair.of(() -> startColor, () -> endColor);
	}
	
}
