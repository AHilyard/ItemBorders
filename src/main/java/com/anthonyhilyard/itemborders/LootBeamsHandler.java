package com.anthonyhilyard.itemborders;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.util.text.Color;

import com.lootbeams.Configuration;

public class LootBeamsHandler
{
	public static Map<String, Color> getCustomBeams()
	{
		Map<String, Color> customBeams = new LinkedHashMap<String, Color>();
		
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
					customBeams.put(name, color);
				}
				// No colon, so we assume this is a mod ID.
				else
				{
					customBeams.put("@" + name, color);
				}
			}
		}

		return customBeams;
	}
}
