package com.anthonyhilyard.itemborders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.anthonyhilyard.iceberg.config.IcebergConfig;
import com.anthonyhilyard.iceberg.config.IcebergConfigSpec;
import com.anthonyhilyard.iceberg.util.ItemColor;
import com.anthonyhilyard.iceberg.util.Selectors;
import com.anthonyhilyard.iceberg.util.Selectors.SelectorDocumentation;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
	private final ConfigValue<UnmodifiableConfig> manualBorders;

	private record ItemKey(Item item, CompoundTag tag) {}

	private Map<ItemKey, TextColor> cachedCustomBorders = new HashMap<ItemKey, TextColor>();
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

		// Build the comment for manual borders.
		StringBuilder selectorsComment = new StringBuilder(" Custom border colors for specific items. Format: { <color> = [\"list of selectors\"] }. Selectors supported:\n");
		for (SelectorDocumentation doc : Selectors.selectorDocumentation())
		{
			selectorsComment.append("    ").append(doc.name()).append(" - ").append(doc.description());

			if (!doc.examples().isEmpty())
			{
				selectorsComment.append("  Examples: ");
				for (int i = 0; i < doc.examples().size(); i++)
				{
					if (i > 0)
					{
						selectorsComment.append(", ");
					}
					selectorsComment.append("\"").append(doc.examples().get(i)).append("\"");
				}
			}
			selectorsComment.append("\n");
		}

		// Remove the final newline.
		selectorsComment.setLength(selectorsComment.length() - 1);

		manualBorders = build.comment(selectorsComment.toString()).defineSubconfig("manual_borders", emptySubconfig, k -> validateColor(k), v -> Selectors.validateSelector((String)v));

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
		TextColor color = null;
		if (value instanceof String string)
		{
			// Parse string color.
			String colorString = string.toLowerCase().replace("0x", "").replace("#", "");
			color = TextColor.parseColor(colorString);
			if (color == null)
			{
				if (colorString.length() == 6 || colorString.length() == 8)
				{
					color = TextColor.parseColor("#" + colorString);
				}
			}
		}
		else if (value instanceof Number number)
		{
			color = TextColor.fromRgb(number.intValue());
		}

		// If alpha is 0 but the color isn't 0x00000000, assume alpha is intended to be 0xFF.
		// Only downside is if users want black borders they'd have to specify "0xFF000000".
		if (color != null && color.getValue() > 0 && color.getValue() <= 0xFFFFFF)
		{
			color = TextColor.fromRgb(color.getValue() | (0xFF << 24));
		}
		
		return color;
	}

	private static boolean validateColor(Object value)
	{
		return getColor(value) != null;
	}

	public TextColor getBorderColorForItem(ItemStack item)
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

		TextColor color = null;

		// Assign an automatic color based on rarity and custom name colors.
		if (ItemBordersConfig.INSTANCE.automaticBorders.get())
		{
			color = ItemColor.getColorForItem(item, null);
		}

		cachedCustomBorders.put(itemKey, color);
		return color;
	}

	@Override
	protected <I extends IcebergConfig<?>> void setInstance(I instance)
	{
		INSTANCE = (ItemBordersConfig) instance;
	}
}