package com.anthonyhilyard.itemborders;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmlclient.gui.GuiUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemBorders
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();

	public void onClientSetup(FMLClientSetupEvent event)
	{
		LOGGER.warn("Item Borders mod is using coremods to modify net/minecraft/client/gui/Gui.renderSlot and net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.renderSlot!  This is not ideal and will be changed to mixins once they are supported for 1.17!");
	}

	public static void renderBorder(PoseStack poseStack, Slot slot)
	{
		// Container GUIs.
		render(poseStack, slot.getItem(), slot.x, slot.y);
	}

	public static void renderBorder(PoseStack poseStack, ItemStack item, int x, int y)
	{
		// If borders are enabled for the hotbar...
		if (ItemBordersConfig.INSTANCE.hotBar.get())
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

		if (item.getRarity() == Rarity.COMMON && !ItemBordersConfig.INSTANCE.showForCommon.get())
		{
			return;
		}

		RenderSystem.setShader(GameRenderer::getPositionTexShader);

		TextColor color = item.getDisplayName().getStyle().getColor();

		// Some mods override the item's getName method to apply different rarity colors,
		// so let's use that color if it is available.
		if (item.getItem() != null &&
			item.getItem().getName(item) != null &&
			item.getItem().getName(item).getStyle() != null &&
			item.getItem().getName(item).getStyle().getColor() != null)
		{
			color = item.getItem().getName(item).getStyle().getColor();
		}

		// If the color is null, default to white.
		if (color == null)
		{
			color = TextColor.fromLegacyFormat(ChatFormatting.WHITE);
		}

		// Bail if this item has white text and we are ignoring "common" rarities.
		// I have to do it by color instead of rarity since modded items often use common rarities in conjunction with modded rarity levels.
		if (color.getValue() == ChatFormatting.WHITE.getColor() && !ItemBordersConfig.INSTANCE.showForCommon.get())
		{
			return;
		}

		Matrix4f matrix = poseStack.last().pose();

		BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		GuiUtils.drawGradientRect(matrix, -1, x,      y + 1,  x + 1,  y + 15, color.getValue() | 0x00000000, color.getValue() | 0xEE000000);
		GuiUtils.drawGradientRect(matrix, -1, x + 15, y + 1,  x + 16, y + 15, color.getValue() | 0x00000000, color.getValue() | 0xEE000000);

		// Use rounded corners by default.
		if (!ItemBordersConfig.INSTANCE.squareCorners.get())
		{
			GuiUtils.drawGradientRect(matrix, -1, x + 1,  y + 15, x + 15, y + 16, color.getValue() | 0xEE000000, color.getValue() | 0xEE000000);
		}
		else
		{
			GuiUtils.drawGradientRect(matrix, -1, x,  y + 15, x + 16, y + 16, color.getValue() | 0xEE000000, color.getValue() | 0xEE000000);
		}
		bufferSource.endBatch();
	}
}
