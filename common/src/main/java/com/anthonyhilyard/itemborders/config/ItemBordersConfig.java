package com.anthonyhilyard.itemborders.config;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.anthonyhilyard.iceberg.config.IcebergConfig;
import com.anthonyhilyard.iceberg.services.IIcebergConfigSpecBuilder;
import com.anthonyhilyard.iceberg.services.Services;
import com.anthonyhilyard.iceberg.util.Selectors;
import com.anthonyhilyard.iceberg.util.Selectors.SelectorDocumentation;
import com.anthonyhilyard.itemborders.ItemBorders;
import com.anthonyhilyard.prism.item.ItemColors;
import com.anthonyhilyard.prism.util.ConfigHelper;
import com.anthonyhilyard.prism.util.ConfigHelper.ColorFormatDocumentation;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemBordersConfig extends IcebergConfig<ItemBordersConfig>
{
	public static ItemBordersConfig getInstance() { return (ItemBordersConfig)configInstances.get(ItemBorders.MODID); }

	public final Supplier<Boolean> hotBar;
	public final Supplier<Boolean> showForCommon;
	public final Supplier<Boolean> squareCorners;
	public final Supplier<Boolean> fullBorder;
	public final Supplier<Boolean> overItems;
	public final Supplier<Boolean> extraGlow;
	public final Supplier<Boolean> automaticBorders;
	public final Supplier<Boolean> legendaryTooltipsSync;
	private final Supplier<Map<String, Object>> manualBorders;
	private record ItemKey(Item item, DataComponentMap components) {}

	private Map<ItemKey, Pair<Supplier<Integer>, Supplier<Integer>>> cachedCustomBorders = Maps.newHashMap();
	private boolean emptyCache = true;

	public ItemBordersConfig(IIcebergConfigSpecBuilder build)
	{
		build.comment(" If you would like to specify manual borders, add a new manual_borders section at the bottom of the file.\n" +
					  " The format for each color of border is 'color = [\"modid:item1\", \"modid:item2\"]\"]'." +
					  " Replace the color with either a color name (like red or blue), or a RGB / ARGB hex color code like 0xFFFFFF or 0xFF00FFFF.\n" +
					  " Here is a sample you can copy / paste and edit as needed:\n" +
					  "[client.options.manual_borders]\n" +
					  "	red = [\"minecraft:torch\", \"minecraft:stick\"]").push("client").push("options");

		hotBar = build.comment(" If the hotbar should display item borders.").add("hotbar", true);
		showForCommon = build.comment(" If item borders should show for common items.").add("show_for_common", false);
		squareCorners = build.comment(" If the borders should have square corners.").add("square_corners", true);
		fullBorder = build.comment(" If the borders should fully envelop item slots (otherwise they will only show on the bottom portion of the slot).").add("full_border", false);
		overItems = build.comment(" If the borders draw over items instead of under.").add("over_items", false);
		extraGlow = build.comment(" If the borders should have a more prominent glow.").add("extra_glow", false);
		automaticBorders = build.comment(" If automatic borders (based on item rarity) should be enabled.").add("auto_borders", true);
		legendaryTooltipsSync = build.comment(" If enabled and Legendary Tooltips is installed, borders will sync with tooltip border colors.").add("legendary_tooltips_sync", false);

		// Build the comment for manual borders.
		StringBuilder entriesComment = new StringBuilder(" Custom border colors for specific items. Format: { <color> = [\"list of selectors\"] }.\n Color formats supported:\n");
		for (ColorFormatDocumentation doc : ConfigHelper.colorFormatDocumentation(true))
		{
			entriesComment.append("   ").append(doc.name()).append(" - ").append(doc.description().replace("\n\n", "\n").replace("\n", "\n     "));

			if (!doc.examples().isEmpty())
			{
				entriesComment.append("  Examples: ");
				for (int i = 0; i < doc.examples().size(); i++)
				{
					// Workaround a documentation bug in Prism.
					if (doc.examples().get(0).startsWith("Using"))
					{
						if (i == 0)
						{
							entriesComment.setLength(entriesComment.length() - 18);
							entriesComment.append("  ");
						}

						if (i > 1)
						{
							entriesComment.append(", ");
						}
						entriesComment.append(doc.examples().get(i));

						if (i == 0)
						{
							entriesComment.append("     Examples: ");
						}
					}
					else
					{
						if (i > 0)
						{
							entriesComment.append(", ");
						}
						entriesComment.append(doc.examples().get(i));
					}
				}
			}
			entriesComment.append("\n");
		}
		entriesComment.append(" Selectors supported:\n");
		for (SelectorDocumentation doc : Selectors.selectorDocumentation())
		{
			entriesComment.append("   ").append(doc.name()).append(" - ").append(doc.description());

			if (!doc.examples().isEmpty())
			{
				entriesComment.append("  Examples: ");
				for (int i = 0; i < doc.examples().size(); i++)
				{
					if (i > 0)
					{
						entriesComment.append(", ");
					}
					entriesComment.append("\"").append(doc.examples().get(i)).append("\"");
				}
			}
			entriesComment.append("\n");
		}

		// Remove the final newline.
		entriesComment.setLength(entriesComment.length() - 1);

		manualBorders = build.comment(entriesComment.toString()).addSubconfig("manual_borders", Map.of(), k -> validateColor(k), v -> v instanceof List);

		build.pop().pop();
	}

	@Override
	protected void onReload()
	{
		// Clear the frame level cache in case anything has changed.
		emptyCache = true;
	}

	public static TextColor getColor(Object value)
	{
		return (TextColor)(Object)ConfigHelper.parseColor(value);
	}

	@SuppressWarnings("unused")
	private static boolean validateColor(Object value)
	{
		return getColor(value) != null;
	}

	@SuppressWarnings("unchecked")
	public Pair<Supplier<Integer>, Supplier<Integer>> getBorderColorForItem(ItemStack item, HolderLookup.Provider provider)
	{
		ItemKey itemKey = new ItemKey(item.getItem(), new PatchedDataComponentMap(item.getComponents()));

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

		// Check the manual border map first.
		Map<String, Object> manualBorderMap = manualBorders.get();
		for (String key : manualBorderMap.keySet())
		{
			TextColor color = getColor(key);
			Pair<Supplier<Integer>, Supplier<Integer>> colors = new Pair<Supplier<Integer>,Supplier<Integer>>(() -> color.getValue(), () -> color.getValue());
			if (color == null)
			{
				// This item has an invalid color value, so skip it.
				continue;
			}
			Object value = manualBorderMap.get(key);
			if (value instanceof String)
			{
				if (Selectors.itemMatches(item, (String)value, provider))
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
						if (Selectors.itemMatches(item, (String)stringVal, provider))
						{
							cachedCustomBorders.put(itemKey, colors);
							return colors;
						}
					}
				}
			}
		}

		Pair<Supplier<Integer>, Supplier<Integer>> colors = null;

		// No manually-configured color was found, so check for an NBT tag.
		if (item.has(DataComponents.CUSTOM_DATA))
		{
			CompoundTag tag = item.get(DataComponents.CUSTOM_DATA).copyTag();
			if (tag.contains("itemborders_colors"))
			{
				CompoundTag colorsTag = tag.getCompound("itemborders_colors");
				TextColor topColor = null;
				TextColor bottomColor = null;
				if (colorsTag.contains("top"))
				{
					topColor = getColor(colorsTag.get("top").getAsString());
				}
				if (colorsTag.contains("bottom"))
				{
					bottomColor = getColor(colorsTag.get("bottom").getAsString());
				}

				if (topColor == null)
				{
					topColor = bottomColor;
				}
				if (bottomColor == null)
				{
					bottomColor = topColor;
				}

				if (topColor != null && bottomColor != null)
				{
					final TextColor finalTopColor = topColor;
					final TextColor finalBottomColor = bottomColor;
					colors = new Pair<Supplier<Integer>,Supplier<Integer>>(() -> finalTopColor.getValue(), () -> finalBottomColor.getValue());
					cachedCustomBorders.put(itemKey, colors);
					return colors;
				}
			}
		}

		// Assign an automatic color based on rarity and custom name colors.
		if (ItemBordersConfig.getInstance().automaticBorders.get())
		{
			TextColor color = ItemColors.getColorForItem(item, null);
			colors = new Pair<Supplier<Integer>,Supplier<Integer>>(() -> color.getValue(), () -> color.getValue());
		}

		if (Services.PLATFORM.isModLoaded("legendarytooltips") && ItemBordersConfig.getInstance().legendaryTooltipsSync.get())
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