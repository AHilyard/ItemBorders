package com.anthonyhilyard.itemborders;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmlclient.gui.GuiUtils;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
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

		int color = item.getRarity().color.getColor();

		Matrix4f matrix = poseStack.last().pose();

		BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		GuiUtils.drawGradientRect(matrix, -1, x,      y + 1,  x + 1,  y + 15, color | 0x00000000, color | 0xEE000000);
		GuiUtils.drawGradientRect(matrix, -1, x + 15, y + 1,  x + 16, y + 15, color | 0x00000000, color | 0xEE000000);
		GuiUtils.drawGradientRect(matrix, -1, x + 1,  y + 15, x + 15, y + 16, color | 0xEE000000, color | 0xEE000000);
		bufferSource.endBatch();
	}
}
