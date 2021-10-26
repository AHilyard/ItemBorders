package com.anthonyhilyard.itemborders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonPrimitive;

@Config(name = Loader.MODID)
public class ItemBordersConfig implements ConfigData
{
	@ConfigEntry.Gui.Excluded
	public static ItemBordersConfig INSTANCE;

	public static void init()
	{
		AutoConfig.register(ItemBordersConfig.class, JanksonConfigSerializer::new);
		INSTANCE = AutoConfig.getConfigHolder(ItemBordersConfig.class).getConfig();
	}

	@Comment("If the hotbar should display item borders.")
	public boolean hotBar = true;
	@Comment("If item borders should show for common items.")
	public boolean showForCommon = false;
	@Comment("If the borders should have square corners.")
	public boolean squareCorners = false;
	@Comment("If automatic borders (based on item rarity) should be enabled.")
	public boolean automaticBorders = true;
	@ConfigEntry.Gui.CollapsibleObject
	@Comment("Custom border colors for specific items.  Format: { \"<color>\" = [\"list of item names or tags\"] }.  Example: { \"FCC040\" = [\"minecraft:stick\", \"torch\"] }")
	private Map<String, List<String>> manualBorders = new HashMap<String, List<String>>();

	@ConfigEntry.Gui.Excluded
	private transient Map<ResourceLocation, TextColor> cachedCustomBorders = new HashMap<ResourceLocation, TextColor>();
	@ConfigEntry.Gui.Excluded
	private transient boolean emptyCache = true;

	@Override
	public void validatePostLoad()
	{
		if (!validateManualBorders(manualBorders))
		{
			// Invalid, do something about it.
			Loader.LOGGER.warn("Invalid manual borders found in config!");
		}
	}

	private static void validateItemPath(String path)
	{
		if (!path.startsWith("#") && !Registry.ITEM.containsKey(new ResourceLocation(path)))
		{
			// This isn't a validation failure, just a warning.
			Loader.LOGGER.warn("Item \"{}\" not found when parsing manual border colors!", path);
		}
		else if (path.startsWith("#") && ItemTags.getAllTags().getTag(new ResourceLocation(path.substring(1))) == null)
		{
			// The list of tags may be empty since both configs and tags are loaded during static initialization.
			// If the list ISN'T empty, we can warn about invalid tags.
			if (!ItemTags.getAllTags().getAllTags().isEmpty())
			{
				// This isn't a validation failure, just a warning.
				Loader.LOGGER.warn("Tag \"{}\" not found when parsing manual border colors!", path);
			}
		}
	}

	private static boolean validateManualBorders(Map<String, List<String>> v)
	{
		if (v == null || v.isEmpty())
		{
			return true;
		}

		// Note that if there is a non-null config value, this validation function always returns true because the entire collection is cleared otherwise, which sucks.
		for (String key : v.keySet())
		{
			// Check that the key is in the proper format.
			if (TextColor.parseColor(key) == null)
			{
				// If parsing failed, try again with appending a # first to support hex codes.
				if (TextColor.parseColor("#" + key) == null)
				{
					Loader.LOGGER.warn("Invalid manual border color found: \"{}\".  This value was ignored.", key);
				}
			}

			List<String> valueList = v.get(key);
			
			List<String> convertedList = new ArrayList<String>();
			for (Object val : valueList)
			{
				String stringVal = ((JsonPrimitive)val).asString();
				// Check for item with this path.
				validateItemPath(stringVal);
				convertedList.add(stringVal);
			}
			v.put(key, convertedList);
		}

		return true;
	}

	public static void appendManualBordersFromPath(String path, TextColor color, Map<ResourceLocation, TextColor> map)
	{
		// This is a tag so add all applicable items.
		if (path.startsWith("#"))
		{
			Tag<Item> tag = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(path.substring(1)));
			for (Item item : tag.getValues())
			{
				map.put(Registry.ITEM.getKey(item), color);
			}
		}
		// Just a single item.
		else
		{
			map.put(new ResourceLocation(path), color);
		}
	}

	public Map<ResourceLocation, TextColor> customBorders()
	{
		// Custom border colors need to be lazily loaded since we can't ensure our config is loaded after loot beams (if applicable).
		if (emptyCache)
		{
			emptyCache = false;
			cachedCustomBorders.clear();
			
			// Now do our own manual stuff.
			for (String key : manualBorders.keySet())
			{
				TextColor color = TextColor.parseColor(key);
				if (color == null)
				{
					color = TextColor.parseColor("#" + key);
					if (color == null)
					{
						// This item has an invalid color value, so skip it.
						continue;
					}
				}

				List<String> valueList = manualBorders.get(key);

				for (Object stringVal : valueList)
				{
					if (stringVal instanceof String)
					{
						appendManualBordersFromPath((String)stringVal, color, cachedCustomBorders);
					}
				}
			}
		}

		return cachedCustomBorders;
	}
}