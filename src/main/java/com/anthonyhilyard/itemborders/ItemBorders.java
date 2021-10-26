package com.anthonyhilyard.itemborders;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.anthonyhilyard.iceberg.util.GuiHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;

public class ItemBorders implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		ItemBordersConfig.init();
	}

	public static void renderBorder(PoseStack poseStack, Slot slot)
	{
		// Container GUIs.
		render(poseStack, slot.getItem(), slot.x, slot.y);
	}

	public static void renderBorder(PoseStack poseStack, ItemStack item, int x, int y)
	{
		// If borders are enabled for the hotbar...
		if (ItemBordersConfig.INSTANCE.hotBar)
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

		// Grab the standard display color.  This generally will be the rarity color.
		TextColor color = TextColor.fromLegacyFormat(ChatFormatting.WHITE);

		// Assign an automatic color based on rarity and custom name colors.
		if (ItemBordersConfig.INSTANCE.automaticBorders)
		{
			color = item.getDisplayName().getStyle().getColor();

			// Some mods override the getName() method of the Item class, so grab that color if it's there.
			if (item.getItem() != null &&
				item.getItem().getName(item) != null &&
				item.getItem().getName(item).getStyle() != null &&
				item.getItem().getName(item).getStyle().getColor() != null)
			{
				color = item.getItem().getName(item).getStyle().getColor();
			}

			// Finally, if the item has a special hover name color (Stored in NBT), use that.
			if (!item.getHoverName().getStyle().isEmpty() && item.getHoverName().getStyle().getColor() != null)
			{
				color = item.getHoverName().getStyle().getColor();
			}
		}

		// Use manually-specified color if available.
		TextColor customColor = ItemBordersConfig.INSTANCE.customBorders().get(Registry.ITEM.getKey(item.getItem()));
		if (customColor != null)
		{
			color = customColor;
		}

		// If the color is null, default to white.
		if (color == null)
		{
			color = TextColor.fromLegacyFormat(ChatFormatting.WHITE);
		}

		if (color.getValue() == ChatFormatting.WHITE.getColor() && !ItemBordersConfig.INSTANCE.showForCommon)
		{
			return;
		}

		RenderSystem.disableDepthTest();

		poseStack.pushPose();
		poseStack.translate(0, 0, 100);
		Matrix4f matrix = poseStack.last().pose();

		BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		GuiHelper.drawGradientRect(matrix, -1, x,      y + 1,  x + 1,  y + 15, color.getValue() | 0x00000000, color.getValue() | 0xEE000000);
		GuiHelper.drawGradientRect(matrix, -1, x + 15, y + 1,  x + 16, y + 15, color.getValue() | 0x00000000, color.getValue() | 0xEE000000);

		// Use rounded corners by default.
		if (!ItemBordersConfig.INSTANCE.squareCorners)
		{
			GuiHelper.drawGradientRect(matrix, -1, x + 1,  y + 15, x + 15, y + 16, color.getValue() | 0xEE000000, color.getValue() | 0xEE000000);
		}
		// Square looks pretty good too though.
		else
		{
			GuiHelper.drawGradientRect(matrix, -1, x,  y + 15, x + 16, y + 16, color.getValue() | 0xEE000000, color.getValue() | 0xEE000000);
		}

		bufferSource.endBatch();
		poseStack.popPose();
	}
}
