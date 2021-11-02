package com.anthonyhilyard.itemborders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.anthonyhilyard.iceberg.util.Selectors;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlFormat;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent;

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
	public final BooleanValue automaticBorders;
	private final ConfigValue<Config> manualBorders;

	private Map<ItemStack, TextColor> cachedCustomBorders = new HashMap<ItemStack, TextColor>();
	private boolean emptyCache = true;

	public ItemBordersConfig(ForgeConfigSpec.Builder build)
	{
		build.comment("Client Configuration").push("client").push("options");

		hotBar = build.comment(" If the hotbar should display item borders.").define("hotbar", true);
		showForCommon = build.comment(" If item borders should show for common items.").define("show_for_common", false);
		squareCorners = build.comment(" If the borders should have square corners.").define("square_corners", true);
		automaticBorders = build.comment(" If automatic borders (based on item rarity) should be enabled.").define("auto_borders", true);
		manualBorders = build.comment(" Custom border colors for specific items. Format: { <color> = [\"list of selectors\"] }. Selectors supported:\n" +
									  "    Item name - Use item name for vanilla items or include mod name for modded items.  Examples: minecraft:stick, iron_ore\n" +
									  "    Tag - $ followed by tag name.  Examples: $forge:stone or $planks\n" +
									  "    Mod name - @ followed by mod identifier.  Examples: @spoiledeggs\n" +
									  "    Rarity - ! followed by item's rarity.  This is ONLY vanilla rarities.  Examples: !uncommon, !rare, !epic\n" +
									  "    Item name color - # followed by color hex code, the hex code must match exactly.  Examples: #23F632\n" +
									  "    Display name - % followed by any text.  Will match any item with this text in its tooltip display name.  Examples: %[Uncommon]\n" +
									  "    Tooltip text - ^ followed by any text.  Will match any item with this text anywhere in the tooltip text (besides the name).  Examples: ^Rarity: Legendary").define("manual_borders", Config.of(TomlFormat.instance()), (v) -> true);
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

		Map<String, Object> manualBorderMap = manualBorders.get().valueMap();
		for (String key : manualBorderMap.keySet())
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

			Object value = manualBorderMap.get(key);

			if (value instanceof String)
			{
				if (Selectors.itemMatches(item, (String)value))
				{
					cachedCustomBorders.put(item, color);
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
							cachedCustomBorders.put(item, color);
							return color;
						}
					}
				}
			}
		}

		cachedCustomBorders.put(item, null);
		return null;
	}
}