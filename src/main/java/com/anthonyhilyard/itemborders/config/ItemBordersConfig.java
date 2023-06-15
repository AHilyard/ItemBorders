package com.anthonyhilyard.itemborders.config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.anthonyhilyard.itemborders.ItemBorders;
import com.anthonyhilyard.itemborders.util.ColorUtil;
import com.anthonyhilyard.itemborders.util.ItemColor;
import com.anthonyhilyard.itemborders.util.Selectors;
import com.anthonyhilyard.itemborders.util.TextColor;
import com.google.common.collect.Maps;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = ItemBorders.MODID)
public class ItemBordersConfig extends Configuration
{
	public static ItemBordersConfig INSTANCE;

	public boolean hotBar;
	public boolean showForCommon;
	public boolean squareCorners;
	public boolean fullBorder;
	public boolean overItems;
	public boolean extraGlow;
	public boolean automaticBorders;
	public boolean legendaryTooltipsSync;
	private Map<String, List<String>> manualBorders = Maps.newHashMap();

	private class ItemKey
	{
		private final Item item;
		private final int metadata;
		private final NBTTagCompound tag;

		public ItemKey(Item item, int metadata, NBTTagCompound tag)
		{
			this.item = item;
			this.metadata = metadata;
			this.tag = tag;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}

			if (obj == null || getClass() != obj.getClass())
			{
				return false;
			}

			ItemKey itemKey = (ItemKey) obj;
			return Objects.equals(item, itemKey.item) &&
				   Objects.equals(metadata, itemKey.metadata) &&
				   Objects.equals(tag, itemKey.tag);
		}

		@Override
		public int hashCode() { return Objects.hash(item, metadata, tag); }
	}

	private Map<ItemKey, Pair<Supplier<Integer>, Supplier<Integer>>> cachedCustomBorders = new HashMap<ItemKey, Pair<Supplier<Integer>, Supplier<Integer>>>();
	private boolean emptyCache = true;

	public static void loadConfig(File file)
	{
		INSTANCE = new ItemBordersConfig(file);
	}

	public ItemBordersConfig(File file)
	{
		super(file);
		load();

		// Update the data type of the categories collection so it maintains the proper order.
		try
		{
			Field categoriesField = Configuration.class.getDeclaredField("categories");
			categoriesField.setAccessible(true);
			Map<String, ConfigCategory> categories = new LinkedHashMap<>();

			// Get or create all the categories in the proper order by getting them here.
			categories.put("options", getCategory("options"));
			categories.put("manual_borders", getCategory("manual_borders"));

			categoriesField.set(this, categories);
		}
		catch (Exception e)
		{
			ItemBorders.LOGGER.error(e);
		}

		hotBar = getBoolean("hotbar", "options", true, "If the hotbar should display item borders.");
		showForCommon = getBoolean("show_for_common", "options", false, "If item borders should show for common items.");
		squareCorners = getBoolean("square_corners", "options", true, "If the borders should have square corners.");
		fullBorder = getBoolean("full_border", "options", false, "If the borders should fully envelop item slots (otherwise they will only show on the bottom portion of the slot).");
		overItems = getBoolean("over_items", "options", false, "If the borders draw over items instead of under.");
		extraGlow = getBoolean("extra_glow", "options", false, "If the borders should have a more prominent glow.");
		automaticBorders = getBoolean("auto_borders", "options", true, "If automatic borders (based on item rarity) should be enabled.");
		legendaryTooltipsSync = getBoolean("legendary_tooltips_sync", "options", false, "If enabled and Legendary Tooltips is installed, borders will sync with tooltip border colors.");


		// Find all manually-defined borders.
		ConfigCategory manualCategory = getCategory("manual_borders");
		manualCategory.setComment("Custom border colors for specific items. Selectors supported:\n" +
									  "    Item name - Use item name for vanilla items or include mod name for modded items, supports metadata (requires mod name when specifying metadata).  Examples: minecraft:stick, iron_ore, minecraft:wool:14\n" +
									  "    Tag - $ followed by ore dictionary name.  Examples: $plankWood or $oreIron\n" +
									  "    Mod name - @ followed by mod identifier.  Examples: @spoiledeggs\n" +
									  "    Rarity - ! followed by item's rarity.  This is ONLY vanilla rarities.  Examples: !uncommon, !rare, !epic\n" +
									  "    Display name - % followed by any text.  Will match any item with this text in its tooltip display name.  Case sensitive.  Examples: %[Uncommon]\n" +
									  "    Tooltip text - ^ followed by any text.  Will match any item with this text anywhere in the tooltip text (besides the name).  Case sensitive.\n\n" +
									  "Color can be specified as a minecraft color name or a 6-digit hexadecimal color code.\n" +
									  "Use the following as an example, add one section for each border color you want: \n" +
									  "S:dark_red <\n" +
									  "        minecraft:wool:14\n" +
									  "        $plankWood\n" +
									  "      >\n" +
									  "S:15EDAC <\n" +
									  "        @spoiledeggs\n" +
									  "        !uncommon\n" +
									  "      >");

		for (String color : manualCategory.getValues().keySet())
		{
			// Each value here is a list of selectors for the given color key.
			if (manualCategory.getValues().get(color).isList() && manualCategory.getValues().get(color).getStringList().length > 0 && ColorUtil.parseColor(color) != null)
			{
				manualBorders.put(color, Arrays.asList(manualCategory.getValues().get(color).getStringList()));
			}
		}

		save();
	}

	@SubscribeEvent
	public static void onLoad(final ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if (event.getModID().equals(ItemBorders.MODID))
		{
			ConfigManager.sync(ItemBorders.MODID, Config.Type.INSTANCE);

			// Clear the frame level cache in case anything has changed.
			INSTANCE.emptyCache = true;
		}
	}

	@SuppressWarnings("unchecked")
	public Pair<Supplier<Integer>, Supplier<Integer>> getBorderColorForItem(ItemStack item)
	{
		ItemKey itemKey = new ItemKey(item.getItem(), item.getMetadata(), item.getTagCompound());

		// Clear the cache first if we have to.
		if (emptyCache)
		{
			emptyCache = false;
			cachedCustomBorders.clear();
		}

		if (cachedCustomBorders.containsKey(itemKey))
		{
			return cachedCustomBorders.get(itemKey);
		}

		for (String key : manualBorders.keySet())
		{
			Integer color = ColorUtil.parseColor(key);
			Pair<Supplier<Integer>, Supplier<Integer>> colors = Pair.of(() -> color, () -> color);
			if (color == null)
			{
				// This item has an invalid color value, so skip it.
				continue;
			}

			Object value = manualBorders.get(key);

			if (value instanceof String)
			{
				if (Selectors.itemMatches(item, (String)value))
				{
					cachedCustomBorders.put(itemKey, colors);
					return colors;
				}
			}
			else if (value instanceof List<?>)
			{
				List<?> valueList = (List<?>) value;
				for (Object stringVal : valueList)
				{
					if (stringVal instanceof String)
					{
						if (Selectors.itemMatches(item, (String)stringVal))
						{
							cachedCustomBorders.put(itemKey, colors);
							return colors;
						}
					}
				}
			}
		}

		Pair<Supplier<Integer>, Supplier<Integer>> colors = null;

		// Assign an automatic color based on rarity and custom name colors.
		if (ItemBordersConfig.INSTANCE.automaticBorders)
		{
			Integer color = ItemColor.getColorForItem(item, TextColor.parseColor("white"));
			colors = Pair.of(() -> color, () -> color);
		}

		if (Loader.isModLoaded("legendarytooltips") && ItemBordersConfig.INSTANCE.legendaryTooltipsSync)
		{
			Pair<Supplier<Integer>, Supplier<Integer>> borderColors = null;

			try
			{
				borderColors = (Pair<Supplier<Integer>, Supplier<Integer>>) Class.forName("com.anthonyhilyard.itemborders.compat.LegendaryTooltipsHandler").getMethod("getBorderColors", ItemStack.class).invoke(null, item);
			}
			catch (Exception e) {}
			if (borderColors != null)
			{
				colors = borderColors;
			}
		}

		cachedCustomBorders.put(itemKey, colors);
		return colors;
	}
}