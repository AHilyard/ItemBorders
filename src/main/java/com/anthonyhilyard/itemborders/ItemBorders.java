package com.anthonyhilyard.itemborders;

import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

public class ItemBorders
{
	public static void renderBorder(MatrixStack matrixStack, Slot slot)
	{
		// Container GUIs.
		render(matrixStack, slot.getItem(), slot.x, slot.y);
	}

	public static void renderBorder(ItemStack item, int x, int y)
	{
		// If borders are enabled for the hotbar...
		if (ItemBordersConfig.INSTANCE.hotBar.get())
		{
			render(new MatrixStack(), item, x, y);
		}
	}

	private static void render(MatrixStack matrixStack, ItemStack item, int x, int y)
	{
		if (item.isEmpty())
		{
			return;
		}

		Color color = ItemBordersConfig.INSTANCE.getBorderColorForItem(item);

		// If the color is null, default to white.
		if (color == null)
		{
			color = Color.fromLegacyFormat(TextFormatting.WHITE);
		}

		if (color.getValue() == TextFormatting.WHITE.getColor() && !ItemBordersConfig.INSTANCE.showForCommon.get())
		{
			return;
		}

		RenderSystem.disableDepthTest();

		matrixStack.pushPose();
		matrixStack.translate(0, 0, ItemBordersConfig.INSTANCE.overItems.get() ? 290 : 100);
		Matrix4f matrix = matrixStack.last().pose();

		int startColor = color.getValue() | 0xEE000000;
		int endColor = color.getValue() & 0x00FFFFFF;

		int topColor = ItemBordersConfig.INSTANCE.fullBorder.get() ? startColor : endColor;
		int bottomColor = startColor;

		int xOffset = ItemBordersConfig.INSTANCE.squareCorners.get() ? 0 : 1;

		IRenderTypeBuffer.Impl bufferSource = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
		GuiUtils.drawGradientRect(matrix, -1, x,      y + 1,  x + 1,  y + 15, topColor, bottomColor);
		GuiUtils.drawGradientRect(matrix, -1, x + 15, y + 1,  x + 16, y + 15, topColor, bottomColor);

		GuiUtils.drawGradientRect(matrix, -1, x + xOffset,  y, x + 16 - xOffset, y + 1, topColor, topColor);
		GuiUtils.drawGradientRect(matrix, -1, x + xOffset,  y + 15, x + 16 - xOffset, y + 16, bottomColor, bottomColor);

		if (ItemBordersConfig.INSTANCE.extraGlow.get())
		{
			int topAlpha = ((topColor >> 24) & 0xFF) / 3;
			int bottomAlpha = ((bottomColor >> 24) & 0xFF) / 3;

			int topGlowColor = (topAlpha << 24) | (topColor & 0x00FFFFFF);
			int bottomGlowColor = (bottomAlpha << 24) | (bottomColor & 0x00FFFFFF);

			GuiUtils.drawGradientRect(matrix, -1, x + 1,      y + 1,  x + 2,  y + 15, topGlowColor, bottomGlowColor);
			GuiUtils.drawGradientRect(matrix, -1, x + 14, y + 1,  x + 15, y + 15, topGlowColor, bottomGlowColor);

			GuiUtils.drawGradientRect(matrix, -1, x + 1,  y + 1, x + 15, y + 2, topGlowColor, topGlowColor);
			GuiUtils.drawGradientRect(matrix, -1, x + 1,  y + 14, x + 15, y + 15, bottomGlowColor, bottomGlowColor);
		}

		bufferSource.endBatch();
		matrixStack.popPose();
	}
}
