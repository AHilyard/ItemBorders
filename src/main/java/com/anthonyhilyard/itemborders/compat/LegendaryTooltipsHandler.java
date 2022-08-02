package com.anthonyhilyard.itemborders.compat;

import java.util.List;
import java.util.function.Supplier;

import com.anthonyhilyard.legendarytooltips.LegendaryTooltipsConfig;
import com.anthonyhilyard.legendarytooltips.LegendaryTooltipsConfig.FrameDefinition;
import com.anthonyhilyard.prism.text.DynamicColor;
import com.anthonyhilyard.prism.util.ConfigHelper;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.item.ItemStack;

public class LegendaryTooltipsHandler
{
	public static Pair<Supplier<Integer>, Supplier<Integer>> getBorderColors(ItemStack item)
	{
		FrameDefinition frameDefinition = LegendaryTooltipsConfig.INSTANCE.getFrameDefinition(item);
		if (frameDefinition.index() < 0)
		{
			return null;
		}

		return new Pair<Supplier<Integer>, Supplier<Integer>>(
			() -> ConfigHelper.applyModifiers(List.of("+s30", "+v20"), DynamicColor.fromRgb(frameDefinition.startBorder().get())).getValue(),
			() -> ConfigHelper.applyModifiers(List.of("+s30", "+v20"), DynamicColor.fromRgb(frameDefinition.endBorder().get())).getValue());
	}
	
}
