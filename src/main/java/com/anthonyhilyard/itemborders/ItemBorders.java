package com.anthonyhilyard.itemborders;

import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.anthonyhilyard.itemborders.config.ItemBordersConfig;
import com.anthonyhilyard.itemborders.util.TextColor;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;


@Mod(modid=ItemBorders.MODID, name=ItemBorders.MODNAME, version=ItemBorders.MODVERSION, acceptedMinecraftVersions = "[1.12.2]", clientSideOnly = true)
@EventBusSubscriber(modid = ItemBorders.MODID, value = Side.CLIENT)
public class ItemBorders
{
	public static final String MODID = "itemborders";
	public static final String MODNAME = "Item Borders";
	public static final String MODVERSION = "1.2.0";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ItemBordersConfig.loadConfig(event.getSuggestedConfigurationFile());
	}

	public static void renderBorder(Slot slot)
	{
		// Container GUIs.
		render(slot.getStack(), slot.xPos, slot.yPos);
	}

	public static void renderBorder(ItemStack item, int x, int y)
	{
		// If borders are enabled for the hotbar...
		if (ItemBordersConfig.INSTANCE.hotBar)
		{
			render(item, x, y);
		}
	}

	@SuppressWarnings("null")
	private static void render(ItemStack item, int x, int y)
	{
		if (item.isEmpty())
		{
			return;
		}

		Pair<Supplier<Integer>, Supplier<Integer>> borderColors = ItemBordersConfig.INSTANCE.getBorderColorForItem(item);

		// If the color is null, default to white.
		if (borderColors == null)
		{
			borderColors = Pair.of(() -> TextColor.parseColor("white"), () -> TextColor.parseColor("white"));
		}

		if ((borderColors.getLeft().get() & 0x00FFFFFF) == TextColor.parseColor("white") &&
			(borderColors.getRight().get() & 0x00FFFFFF) == TextColor.parseColor("white") &&
			!ItemBordersConfig.INSTANCE.showForCommon)
		{
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, ItemBordersConfig.INSTANCE.overItems ? 290 : 100);

		int startColor = borderColors.getLeft().get() & 0x00FFFFFF;
		int endColor = borderColors.getRight().get() & 0x00FFFFFF;

		int topColor = ItemBordersConfig.INSTANCE.fullBorder ? startColor | 0xEE000000 : startColor;
		int bottomColor = endColor | 0xEE000000;

		int xOffset = ItemBordersConfig.INSTANCE.squareCorners ? 0 : 1;

		GuiUtils.drawGradientRect(-1, x,      y + 1,  x + 1,  y + 15, topColor, bottomColor);
		GuiUtils.drawGradientRect(-1, x + 15, y + 1,  x + 16, y + 15, topColor, bottomColor);

		GuiUtils.drawGradientRect(-1, x + xOffset,  y, x + 16 - xOffset, y + 1, topColor, topColor);
		GuiUtils.drawGradientRect(-1, x + xOffset,  y + 15, x + 16 - xOffset, y + 16, bottomColor, bottomColor);

		if (ItemBordersConfig.INSTANCE.extraGlow)
		{
			int topAlpha = ((topColor >> 24) & 0xFF) / 3;
			int bottomAlpha = ((bottomColor >> 24) & 0xFF) / 3;

			int topGlowColor = (topAlpha << 24) | (topColor & 0x00FFFFFF);
			int bottomGlowColor = (bottomAlpha << 24) | (bottomColor & 0x00FFFFFF);

			GuiUtils.drawGradientRect(-1, x + 1,      y + 1,  x + 2,  y + 15, topGlowColor, bottomGlowColor);
			GuiUtils.drawGradientRect(-1, x + 14, y + 1,  x + 15, y + 15, topGlowColor, bottomGlowColor);

			GuiUtils.drawGradientRect(-1, x + 1,  y + 1, x + 15, y + 2, topGlowColor, topGlowColor);
			GuiUtils.drawGradientRect(-1, x + 1,  y + 14, x + 15, y + 15, bottomGlowColor, bottomGlowColor);
		}

		GlStateManager.popMatrix();
	}
}
