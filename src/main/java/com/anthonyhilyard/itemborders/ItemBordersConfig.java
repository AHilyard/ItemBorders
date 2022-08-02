package com.anthonyhilyard.itemborders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.anthonyhilyard.iceberg.config.IcebergConfig;
import com.anthonyhilyard.iceberg.config.IcebergConfigSpec;
import com.anthonyhilyard.iceberg.util.Selectors;
import com.anthonyhilyard.iceberg.util.Selectors.SelectorDocumentation;
import com.anthonyhilyard.prism.item.ItemColors;
import com.anthonyhilyard.prism.util.ConfigHelper;
import com.anthonyhilyard.prism.util.ConfigHelper.ColorFormatDocumentation;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Loader.MODID, bus = Bus.MOD)
public class ItemBordersConfig extends IcebergConfig<ItemBordersConfig>
{
	private static ItemBordersConfig INSTANCE;
	public static ItemBordersConfig getInstance() { return INSTANCE; }

	public final BooleanValue hotBar;
	public final BooleanValue showForCommon;
	public final BooleanValue squareCorners;
	public final BooleanValue fullBorder;
	public final BooleanValue overItems;
	public final BooleanValue extraGlow;
	public final BooleanValue automaticBorders;
	public final BooleanValue legendaryTooltipsSync;
	private final ConfigValue<UnmodifiableConfig> manualBorders;

	private record ItemKey(Item item, CompoundTag tag) {}

	private Map<ItemKey, Pair<Supplier<Integer>, Supplier<Integer>>> cachedCustomBorders = new HashMap<ItemKey, Pair<Supplier<Integer>, Supplier<Integer>>>();
	private boolean emptyCache = true;

	private static final UnmodifiableConfig emptySubconfig = new ForgeConfigSpec.Builder().build();

	public ItemBordersConfig(IcebergConfigSpec.Builder build)
	{
		build.comment("Client Configuration").push("client").push("options");

		hotBar = build.comment(" If the hotbar should display item borders.").define("hotbar", true);
		showForCommon = build.comment(" If item borders should show for common items.").define("show_for_common", false);
		squareCorners = build.comment(" If the borders should have square corners.").define("square_corners", true);
		fullBorder = build.comment(" If the borders should fully envelop item slots (otherwise they will only show on the bottom portion of the slot).").define("full_border", false);
		overItems = build.comment(" If the borders draw over items instead of under.").define("over_items", false);
		extraGlow = build.comment(" If the borders should have a more prominent glow.").define("extra_glow", false);
		automaticBorders = build.comment(" If automatic borders (based on item rarity) should be enabled.").define("auto_borders", true);
		legendaryTooltipsSync = build.comment(" If enabled and Legendary Tooltips is installed, borders will sync with tooltip border colors.").define("legendary_tooltips_sync", false);

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

		manualBorders = build.comment(entriesComment.toString()).defineSubconfig("manual_borders", emptySubconfig, k -> validateColor(k), v -> Selectors.validateSelector((String)v));

		build.pop().pop();
	}

	@SubscribeEvent
	public static void onReload(ModConfigEvent.Reloading e)
	{
		if (e.getConfig().getModId().equals(Loader.MODID))
		{
			// Clear the frame level cache in case anything has changed.
			INSTANCE.emptyCache = true;
		}
	}

	public static TextColor getColor(Object value)
	{
		return (TextColor)(Object)ConfigHelper.parseColor(value);
	}

	private static boolean validateColor(Object value)
	{
		return getColor(value) != null;
	}

	@SuppressWarnings("unchecked")
	public Pair<Supplier<Integer>, Supplier<Integer>> getBorderColorForItem(ItemStack item)
	{
		ItemKey itemKey = new ItemKey(item.getItem(), item.getTag());

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

		Map<String, Object> manualBorderMap = manualBorders.get().valueMap();
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
		if (ItemBordersConfig.INSTANCE.automaticBorders.get())
		{
			TextColor color = ItemColors.getColorForItem(item, null);
			colors = new Pair<Supplier<Integer>,Supplier<Integer>>(() -> color.getValue(), () -> color.getValue());
		}

		if (ModList.get().isLoaded("legendarytooltips") && ItemBordersConfig.INSTANCE.legendaryTooltipsSync.get())
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

	@Override
	protected <I extends IcebergConfig<?>> void setInstance(I instance)
	{
		INSTANCE = (ItemBordersConfig) instance;
	}
}