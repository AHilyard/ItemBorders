package com.anthonyhilyard.itemborders.mixin;

import com.anthonyhilyard.itemborders.ItemBorders;
import com.anthonyhilyard.itemborders.config.ItemBordersConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Gui.class)
public class GuiMixin
{
	// Renders under the item.
	@Inject(method = "renderSlot", at = @At("HEAD"))
	public void renderSlotBackground(GuiGraphics graphics, int x, int y, DeltaTracker tracker, Player player, ItemStack item, int seed, CallbackInfo info)
	{
		if (!ItemBordersConfig.getInstance().overItems.get())
		{
			ItemBorders.renderBorder(graphics, item, x, y);
		}
	}

	// Renders over the item.
	@Inject(method = "renderSlot",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", shift = Shift.AFTER))
	public void renderSlotForeground(GuiGraphics graphics, int x, int y, DeltaTracker tracker, Player player, ItemStack item, int seed, CallbackInfo info)
	{
		if (ItemBordersConfig.getInstance().overItems.get())
		{
			graphics.nextStratum();
			ItemBorders.renderBorder(graphics, item, x, y);
		}
	}
}