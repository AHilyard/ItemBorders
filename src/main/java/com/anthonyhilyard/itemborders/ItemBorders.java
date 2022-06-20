package com.anthonyhilyard.itemborders;

import net.minecraftforge.client.gui.GuiUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;

public class ItemBorders
{
	public static void renderBorder(PoseStack poseStack, Slot slot)
	{
		// Container GUIs.
		render(poseStack, slot.getItem(), slot.x, slot.y);
	}

	public static void renderBorder(PoseStack poseStack, ItemStack item, int x, int y)
	{
		// If borders are enabled for the hotbar...
		if (ItemBordersConfig.getInstance().hotBar.get())
		{
			render(new PoseStack(), item, x, y);
		}
	}

	private static void render(PoseStack poseStack, ItemStack item, int x, int y)
	{
		if (item.isEmpty())
		{
			return;
		}

		TextColor color = ItemBordersConfig.getInstance().getBorderColorForItem(item);

		// If the color is null, default to white.
		if (color == null)
		{
			color = TextColor.fromLegacyFormat(ChatFormatting.WHITE);
		}

		if (color.getValue() == ChatFormatting.WHITE.getColor() && !ItemBordersConfig.getInstance().showForCommon.get())
		{
			return;
		}

		RenderSystem.disableDepthTest();

		poseStack.pushPose();
		poseStack.translate(0, 0, ItemBordersConfig.getInstance().overItems.get() ? 290 : 100);
		Matrix4f matrix = poseStack.last().pose();

		int startColor = color.getValue() | 0xEE000000;
		int endColor = color.getValue() & 0x00FFFFFF;

		int topColor = ItemBordersConfig.getInstance().fullBorder.get() ? startColor : endColor;
		int bottomColor = startColor;

		int xOffset = ItemBordersConfig.getInstance().squareCorners.get() ? 0 : 1;

		BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		GuiUtils.drawGradientRect(matrix, -1, x,      y + 1,  x + 1,  y + 15, topColor, bottomColor);
		GuiUtils.drawGradientRect(matrix, -1, x + 15, y + 1,  x + 16, y + 15, topColor, bottomColor);

		GuiUtils.drawGradientRect(matrix, -1, x + xOffset,  y, x + 16 - xOffset, y + 1, topColor, topColor);
		GuiUtils.drawGradientRect(matrix, -1, x + xOffset,  y + 15, x + 16 - xOffset, y + 16, bottomColor, bottomColor);

		if (ItemBordersConfig.getInstance().extraGlow.get())
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
		poseStack.popPose();
	}
}
