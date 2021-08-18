package com.anthonyhilyard.itemborders;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemBorders
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();

	public void onClientSetup(FMLClientSetupEvent event)
	{
	}

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

		// Grab the standard display color.  This generally will be the rarity color.
		Color color = item.getDisplayName().getStyle().getColor();

		// Some mods override the getName() method of the Item class, so grab that color if it's there.
		if (item.getItem() != null &&
			item.getItem().getName(item) != null &&
			item.getItem().getName(item).getStyle() != null &&
			item.getItem().getName(item).getStyle().getColor() != null)
		{
			color = item.getItem().getName(item).getStyle().getColor();
		}

		// Finally, if the item has a special hover name color (Stored in NBT), use that.
		if (!item.getHoverName().getStyle().isEmpty())
		{
			color = item.getHoverName().getStyle().getColor();
		}

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
		matrixStack.translate(0, 0, 100);
		Matrix4f matrix = matrixStack.last().pose();

		IRenderTypeBuffer.Impl bufferSource = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
		GuiUtils.drawGradientRect(matrix, -1, x,      y + 1,  x + 1,  y + 15, color.getValue() | 0x00000000, color.getValue() | 0xEE000000);
		GuiUtils.drawGradientRect(matrix, -1, x + 15, y + 1,  x + 16, y + 15, color.getValue() | 0x00000000, color.getValue() | 0xEE000000);

		// Use rounded corners by default.
		if (!ItemBordersConfig.INSTANCE.squareCorners.get())
		{
			GuiUtils.drawGradientRect(matrix, -1, x + 1,  y + 15, x + 15, y + 16, color.getValue() | 0xEE000000, color.getValue() | 0xEE000000);
		}
		// Square looks pretty good too though.
		else
		{
			GuiUtils.drawGradientRect(matrix, -1, x,  y + 15, x + 16, y + 16, color.getValue() | 0xEE000000, color.getValue() | 0xEE000000);
		}

		bufferSource.endBatch();
		matrixStack.popPose();
	}
}
