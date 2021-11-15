package com.anthonyhilyard.itemborders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.anthonyhilyard.iceberg.util.Selectors;

import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
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
	@Comment("Custom border colors for specific items.  Format: { \"<color>\": [\"list of selectors\"] }.  Selectors supported:\n" + 
			 "  Item name - Use item name for vanilla items or include mod name for modded items.  Examples: \"minecraft:stick\", \"iron_ore\"\n" +
			 "  Tag - $ followed by tag name.  Examples: \"$minecraft:stone\" or \"$planks\"\n" +
			 "  Mod name - @ followed by mod identifier.  Examples: \"@spoiledeggs\"\n" +
			 "  Rarity - ! followed by item's rarity.  This is ONLY vanilla rarities.  Examples: \"!common\", \"!uncommon\", \"!rare\", \"!epic\"\n" +
			 "  Item name color - # followed by color hex code, the hex code must match exactly.  Examples: \"#23F632\"\n" +
			 "  Display name - % followed by any text.  Will match any item with this text in its tooltip display name.  Examples: \"%[Uncommon]\"\n" +
			 "  Tooltip text - ^ followed by any text.  Will match any item with this text anywhere in the tooltip text (besides the name).  Examples: \"^Legendary\"")
	private Map<String, List<String>> manualBorders = new LinkedHashMap<String, List<String>>();

	@ConfigEntry.Gui.Excluded
	private transient Map<ItemStack, TextColor> cachedCustomBorders = new HashMap<ItemStack, TextColor>();
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
				if ((key.replace("0x", "").length() == 6 && TextColor.parseColor("#" + key.replace("0x", "")) == null) ||
					(key.replace("0x", "").length() == 8 && TextColor.parseColor("#" + key.toLowerCase().replace("0xff", "")) == null))
				{
					Loader.LOGGER.warn("Invalid manual border color found: \"{}\".  This value was ignored.", key);
				}
			}

			List<String> valueList = v.get(key);
			
			List<String> convertedList = new ArrayList<String>();
			for (Object val : valueList)
			{
				String stringVal = ((JsonPrimitive)val).asString();
				convertedList.add(stringVal);
			}
			v.put(key, convertedList);
		}

		return true;
	}

	public TextColor getBorderColorForItem(ItemStack item)
	{
		// Clear the cache first if we have to.
		if (emptyCache)
		{
			emptyCache = false;
			cachedCustomBorders.clear();
		}

		if (cachedCustomBorders.containsKey(item))
		{
			return cachedCustomBorders.get(item);
		}

		for (String key : manualBorders.keySet())
		{
			TextColor color = TextColor.parseColor(key);
			if (color == null)
			{
				if (key.replace("0x", "").length() == 6)
				{
					color = TextColor.parseColor("#" + key.replace("0x", ""));
				}
				else if (key.replace("0x", "").length() == 8)
				{
					color = TextColor.parseColor("#" + key.toLowerCase().replace("0xff", ""));
				}

				if (color == null)
				{
					// This item has an invalid color value, so skip it.
					continue;
				}
			}

			List<String> selectorList = manualBorders.get(key);

			for (Object selector : selectorList)
			{
				if (selector instanceof String)
				{
					if (Selectors.itemMatches(item, (String)selector))
					{
						cachedCustomBorders.put(item, color);
						return color;
					}
				}
			}
		}

		cachedCustomBorders.put(item, null);
		return null;
	}
}