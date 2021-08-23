package com.anthonyhilyard.itemborders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlFormat;

public class ItemBordersConfig
{
	public static final ForgeConfigSpec SPEC;
	public static final ItemBordersConfig INSTANCE;
	static
	{
		Config.setInsertionOrderPreserved(true);
		Pair<ItemBordersConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ItemBordersConfig::new);
		SPEC = specPair.getRight();
		INSTANCE = specPair.getLeft();
	}

	public final BooleanValue hotBar;
	public final BooleanValue showForCommon;
	public final BooleanValue squareCorners;
	public final BooleanValue automaticBorders;
	private final ConfigValue<Config> manualBorders;
	// public final BooleanValue lootBeamSync;

	private Map<ResourceLocation, Integer> cachedCustomBorders = new HashMap<ResourceLocation, Integer>();
	private boolean emptyCache = true;

	public ItemBordersConfig(ForgeConfigSpec.Builder build)
	{
		build.comment("Client Configuration").push("client").push("options");

		hotBar = build.comment(" If the hotbar should display item borders.").define("hotbar", true);
		showForCommon = build.comment(" If item borders should show for common items.").define("show_for_common", false);
		squareCorners = build.comment(" If the borders should have square corners.").define("square_corners", false);
		automaticBorders = build.comment(" If automatic borders (based on item rarity) should be enabled.").define("auto_borders", true);
		manualBorders = build.comment(" Custom border colors for specific items.  Format: { <color> = \"item name or #tag\", <color> = [\"list of item names or tags\"]}.  Example: { FCC040 = [\"minecraft:stick\", \"torch\"], lime = \"#ores\"]}").define("manual_borders", Config.of(TomlFormat.instance()), (v) -> validateManualBorders((Config)v));
		// lootBeamSync = build.comment(" If border colors should sync with loot beam colors. (From Loot Beams mod--any custom beams colors specified in Loot Beams configuration will be displayed as borders.)").define("loot_beam_sync", true);

		build.pop().pop();
	}

	@SubscribeEvent
	public static void onLoad(ModConfig.Loading e)
	{
		if (e.getConfig().getModId().equals(Loader.MODID))
		{
			// Rebuild border color lists here.
			Loader.LOGGER.info("Advancement Plaques config reloaded.");
		}
		// else if (e.getConfig().getModId().equals("lootbeams"))
		// {
		// 	INSTANCE.emptyCache = true;
		// }
	}

	private static void validateItemPath(String path)
	{
		if (!path.startsWith("#") && !ForgeRegistries.ITEMS.containsKey(new ResourceLocation(path)))
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

	private static Integer parseColor(String name)
	{
		TextFormatting textFormat = TextFormatting.getByName(name);
		if (textFormat == null)
		{
			if ((name.length() == 3 || name.length() == 6 || name.length() == 8) &&
				name.matches("[0-9A-Fa-f]*"))
			{
				return Integer.parseInt(name, 16);
			}
			return null;
		}
		return textFormat.getColor();
	}

	private static boolean validateManualBorders(Config v)
	{
		if (v == null || v.valueMap() == null)
		{
			return false;
		}

		// Note that if there is a non-null config value, this validation function always returns true because the entire collection is cleared otherwise, which sucks.
		for (String key : v.valueMap().keySet())
		{
			// Check that the key is in the proper format.
			if (parseColor(key) == null)
			{
				Loader.LOGGER.warn("Invalid manual border color found: \"{}\".  This value was ignored.", key);
			}

			Object value = v.valueMap().get(key);

			// Value can be a single item or a list of them.
			if (value instanceof String)
			{
				// Check for item with this path.
				validateItemPath((String)value);
			}
			else if (value instanceof List<?>)
			{
				List<?> valueList = (List<?>) value;
				for (Object stringVal : valueList)
				{
					if (stringVal instanceof String)
					{
						// Check for item with this path.
						validateItemPath((String)stringVal);
					}
					else
					{
						Loader.LOGGER.warn("Invalid manual border item path or tag found: \"{}\".  This value was ignored.", stringVal);
					}
				}
			}
			else
			{
				Loader.LOGGER.warn("Invalid manual border item path or tag found: \"{}\".  This value was ignored.", value);
			}
		}

		// Empty the cache so it is regenerated on the next border draw.
		INSTANCE.emptyCache = true;
		return true;
	}

	public static void appendManualBordersFromPath(String path, int color, Map<ResourceLocation, Integer> map)
	{
		// This is a tag so add all applicable items.
		if (path.startsWith("#"))
		{
			Tag<Item> tag = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(path.substring(1)));
			for (Item item : tag.getValues())
			{
				map.put(item.getRegistryName(), color);
			}
		}
		// Just a single item.
		else
		{
			map.put(new ResourceLocation(path), color);
		}
	}

	public Map<ResourceLocation, Integer> customBorders()
	{
		// Custom border colors need to be lazily loaded since we can't ensure our config is loaded after loot beams (if applicable).
		if (emptyCache)
		{
			emptyCache = false;
			cachedCustomBorders.clear();
			
			// Generate custom borders now.  Start with Loot Beams stuff.
			// if (lootBeamSync.get() && ModList.get().isLoaded("lootbeams"))
			// {
			// 	try
			// 	{
			// 		cachedCustomBorders.putAll((Map<ResourceLocation, Color>)Class.forName("com.anthonyhilyard.itemborders.LootBeamsHandler").getMethod("getCustomBeams").invoke(null, new Object[]{}));
			// 	}
			// 	catch (Exception e)
			// 	{
			// 		Loader.LOGGER.warn("Failed to synchronize Loot Beams!");
			// 	}
			// }

			// Now do our own manual stuff.
			Map<String, Object> manualBorderMap = manualBorders.get().valueMap();
			for (String key : manualBorderMap.keySet())
			{
				Integer color = parseColor(key);
				if (color == null)
				{
					// This item has an invalid color value, so skip it.
					continue;
				}

				Object value = manualBorderMap.get(key);

				// Value can be a single item/tag or a list of them.
				if (value instanceof String)
				{
					appendManualBordersFromPath((String)value, color, cachedCustomBorders);
				}
				else if (value instanceof List<?>)
				{
					List<?> valueList = (List<?>) value;
					for (Object stringVal : valueList)
					{
						if (stringVal instanceof String)
						{
							appendManualBordersFromPath((String)stringVal, color, cachedCustomBorders);
						}
					}
				}
			}
		}

		return cachedCustomBorders;
	}

}