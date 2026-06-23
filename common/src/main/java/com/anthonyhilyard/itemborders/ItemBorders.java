package com.anthonyhilyard.itemborders;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.anthonyhilyard.iceberg.util.GuiHelper;
import com.anthonyhilyard.itemborders.config.ItemBordersConfig;
import com.mojang.datafixers.util.Pair;

public class ItemBorders
{
	public static final String MODID = "itemborders";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static void init()
	{
		ItemBordersConfig.register(ItemBordersConfig.class, MODID);
	}

	public static void renderBorder(GuiGraphicsExtractor graphics, Slot slot)
	{
		render(graphics, slot.getItem(), slot.x, slot.y);
	}

	public static void renderBorder(GuiGraphicsExtractor graphics, ItemStack item, int x, int y)
	{
		if (ItemBordersConfig.getInstance().hotBar.get())
		{
			render(graphics, item, x, y);
		}
	}

	private static void render(GuiGraphicsExtractor graphics, ItemStack item, int x, int y)
	{
		if (item.isEmpty())
		{
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null)
		{
			return;
		}

		Pair<Supplier<Integer>, Supplier<Integer>> borderColors = ItemBordersConfig.getInstance().getBorderColorForItem(item, minecraft.level.registryAccess());

		if (borderColors == null)
		{
			borderColors = new Pair<Supplier<Integer>, Supplier<Integer>>(() -> TextColor.fromLegacyFormat(ChatFormatting.WHITE).getValue(),
																		  () -> TextColor.fromLegacyFormat(ChatFormatting.WHITE).getValue());
		}

		if ((borderColors.getFirst().get() & 0x00FFFFFF)  == ChatFormatting.WHITE.getColor() &&
			(borderColors.getSecond().get() & 0x00FFFFFF) == ChatFormatting.WHITE.getColor() &&
			!ItemBordersConfig.getInstance().showForCommon.get())
		{
			return;
		}

		int startColor = borderColors.getFirst().get() & 0x00FFFFFF;
		int endColor = borderColors.getSecond().get() & 0x00FFFFFF;

		int topColor = ItemBordersConfig.getInstance().fullBorder.get() ? startColor | 0xEE000000 : startColor;
		int bottomColor = endColor | 0xEE000000;

		int xOffset = ItemBordersConfig.getInstance().squareCorners.get() ? 0 : 1;

		// Renders left and right borders.
		GuiHelper.drawGradientRect(graphics, x,      y + 1,  x + 1,  y + 15, topColor, bottomColor);
		GuiHelper.drawGradientRect(graphics, x + 15, y + 1,  x + 16, y + 15, topColor, bottomColor);

		// Renders top and bottom borders.
		GuiHelper.drawGradientRect(graphics, x + xOffset,  y,      x + 16 - xOffset, y + 1,  topColor, topColor);
		GuiHelper.drawGradientRect(graphics, x + xOffset,  y + 15, x + 16 - xOffset, y + 16, bottomColor, bottomColor);

		if (ItemBordersConfig.getInstance().extraGlow.get())
		{
			int topAlpha = ((topColor >> 24) & 0xFF) / 3;
			int bottomAlpha = ((bottomColor >> 24) & 0xFF) / 3;

			int topGlowColor = (topAlpha << 24) | (topColor & 0x00FFFFFF);
			int bottomGlowColor = (bottomAlpha << 24) | (bottomColor & 0x00FFFFFF);

			// Renders left and right glow.
			GuiHelper.drawGradientRect(graphics, x + 1,  y + 1,  x + 2,  y + 15, topGlowColor, bottomGlowColor);
			GuiHelper.drawGradientRect(graphics, x + 14, y + 1,  x + 15, y + 15, topGlowColor, bottomGlowColor);

			// Renders top and bottom glow.
			GuiHelper.drawGradientRect(graphics, x + 1,  y + 1,  x + 15, y + 2,  topGlowColor, topGlowColor);
			GuiHelper.drawGradientRect(graphics, x + 1,  y + 14, x + 15, y + 15, bottomGlowColor, bottomGlowColor);
		}
	}
}