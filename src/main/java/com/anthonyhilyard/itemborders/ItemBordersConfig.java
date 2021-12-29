package com.anthonyhilyard.itemborders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.anthonyhilyard.iceberg.util.ItemColor;
import com.anthonyhilyard.iceberg.util.Selectors;
import com.anthonyhilyard.iceberg.util.Selectors.SelectorDocumentation;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.Color;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlFormat;

@Mod.EventBusSubscriber(modid = Loader.MODID, bus = Bus.MOD)
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
	public final BooleanValue fullBorder;
	public final BooleanValue overItems;
	public final BooleanValue extraGlow;
	public final BooleanValue automaticBorders;
	private final ConfigValue<Config> manualBorders;
	public final BooleanValue lootBeamSync;

	private static class ItemKey
	{
		public final Item item;
		public final CompoundNBT tag;
		public ItemKey(Item item, CompoundNBT tag)
		{
			this.item = item;
			this.tag = tag;
		}

		@Override
		public int hashCode() { return Objects.hash(item, tag); }

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			else if (!(obj instanceof ItemKey))
			{
				return false;
			}
			else
			{
				ItemKey other = (ItemKey) obj;
				return Objects.equals(item, other.item) &&
					   Objects.equals(tag, other.tag);
			}
		}
	}

	private Map<ItemKey, Color> cachedCustomBorders = new HashMap<ItemKey, Color>();
	private Map<String, Color> lootBeamsSelectors = new HashMap<>();
	private boolean emptyCache = true;

	public ItemBordersConfig(ForgeConfigSpec.Builder build)
	{
		build.comment("Client Configuration").push("client").push("options");

		hotBar = build.comment(" If the hotbar should display item borders.").define("hotbar", true);
		showForCommon = build.comment(" If item borders should show for common items.").define("show_for_common", false);
		squareCorners = build.comment(" If the borders should have square corners.").define("square_corners", true);
		fullBorder = build.comment(" If the borders should fully envelop item slots (otherwise they will only show on the bottom portion of the slot).").define("full_border", false);
		overItems = build.comment(" If the borders draw over items instead of under.").define("over_items", false);
		extraGlow = build.comment(" If the borders should have a more prominent glow.").define("extra_glow", false);
		automaticBorders = build.comment(" If automatic borders (based on item rarity) should be enabled.").define("auto_borders", true);
		lootBeamSync = build.comment(" If border colors should sync with loot beam colors. (From Loot Beams mod--any custom beams colors specified in Loot Beams configuration will be displayed as borders.)").define("loot_beam_sync", true);

		// Build the comment for manual borders.
		StringBuilder bordersComment = new StringBuilder(" Custom border colors for specific items. Format: { <color> = [\"list of selectors\"] }. Selectors supported:\n");
		for (SelectorDocumentation doc : Selectors.selectorDocumentation())
		{
			bordersComment.append("    ").append(doc.name).append(" - ").append(doc.description);
			
			if (!doc.examples.isEmpty())
			{
				bordersComment.append("  Examples: ");
				for (int i = 0; i < doc.examples.size(); i++)
				{
					if (i > 0)
					{
						bordersComment.append(", ");
					}
					bordersComment.append("\"").append(doc.examples.get(i)).append("\"");
				}
			}
			bordersComment.append("\n");
		}

		// Remove the final newline.
		bordersComment.setLength(bordersComment.length() - 1);

		manualBorders = build.comment(bordersComment.toString()).define("manual_borders", Config.of(TomlFormat.instance()), (v) -> true);

		build.pop().pop();
	}

	@SubscribeEvent
	public static void onLoad(ModConfig.Reloading e)
	{
		if (e.getConfig().getModId().equals(Loader.MODID) || e.getConfig().getModId().equals("lootbeams"))
		{
			// Clear the frame level cache in case anything has changed.
			INSTANCE.emptyCache = true;
		}
	}

	@SuppressWarnings("unchecked")
	public Color getBorderColorForItem(ItemStack item)
	{
		ItemKey itemKey = new ItemKey(item.getItem(), item.getTag());

		// Clear the cache first if we have to.
		if (emptyCache)
		{
			emptyCache = false;
			cachedCustomBorders.clear();

			// Update loot beams borders now if needed.
			if (lootBeamSync.get() && ModList.get().isLoaded("lootbeams"))
			{
				try
				{
					lootBeamsSelectors = (Map<String, Color>)Class.forName("com.anthonyhilyard.itemborders.LootBeamsHandler").getMethod("getCustomBeams").invoke(null, new Object[]{});
				}
				catch (Exception e)
				{
					Loader.LOGGER.warn(ExceptionUtils.getStackTrace(e));
				}
			}
		}

		if (cachedCustomBorders.containsKey(itemKey))
		{
			return cachedCustomBorders.get(itemKey);
		}

		// Check lootbeams stuff first.
		if (lootBeamsSelectors != null)
		{
			for (String selector : lootBeamsSelectors.keySet())
			{
				Color color = lootBeamsSelectors.get(selector);
				if (color == null)
				{
					continue;
				}

				if (Selectors.itemMatches(item, selector))
				{
					cachedCustomBorders.put(itemKey, color);
					return color;
				}
			}
		}

		Map<String, Object> manualBorderMap = manualBorders.get().valueMap();
		for (String key : manualBorderMap.keySet())
		{
			Color color = Color.parseColor(key);
			if (color == null)
			{
				if (key.replace("0x", "").length() == 6)
				{
					color = Color.parseColor("#" + key.replace("0x", ""));
				}
				else if (key.replace("0x", "").length() == 8)
				{
					color = Color.parseColor("#" + key.toLowerCase().replace("0xff", ""));
				}

				if (color == null)
				{
					// This item has an invalid color value, so skip it.
					continue;
				}
			}

			Object value = manualBorderMap.get(key);

			if (value instanceof String)
			{
				if (Selectors.itemMatches(item, (String)value))
				{
					cachedCustomBorders.put(itemKey, color);
					return color;
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
							cachedCustomBorders.put(itemKey, color);
							return color;
						}
					}
				}
			}
		}

		Color color = null;

		// Assign an automatic color based on rarity and custom name colors.
		if (ItemBordersConfig.INSTANCE.automaticBorders.get())
		{
			color = ItemColor.getColorForItem(item, null);
		}

		cachedCustomBorders.put(itemKey, color);
		return color;
	}
}