package com.anthonyhilyard.itemborders;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Color;
import net.minecraftforge.registries.ForgeRegistries;

import com.lootbeams.Configuration;

public class LootBeamsHandler
{
	public static Map<ResourceLocation, Color> getCustomBeams()
	{
		Map<ResourceLocation, Color> customBeams = new HashMap<ResourceLocation, Color>();
		
		for (String value : Configuration.COLOR_OVERRIDES.get())
		{
			String[] tokens = value.split("=");
			if (tokens.length == 2)
			{
				String name = tokens[0];
				Color color = null;
				try
				{
					color = Color.fromRgb(java.awt.Color.decode(tokens[1]).getRGB() & 0xFFFFFF);
				}
				catch (NumberFormatException e)
				{
					// The format was invalid, oops. 
				}

				// Invalid color value, just skip it.
				if (color == null)
				{
					continue;
				}

				// Standard modid:item path.
				if (name.contains(":"))
				{
					ItemBordersConfig.appendManualBordersFromPath(name, color, customBeams);
				}
				// No mod ID, so we assume this is a mod ID.
				else
				{
					// Add all items from mod to cache.
					for (ResourceLocation key : ForgeRegistries.ITEMS.getKeys())
					{
						if (key.getNamespace().equals(name))
						{
							customBeams.put(key, color);
						}
					}
				}
			}
		}

		return customBeams;
	}
}
