package com.anthonyhilyard.itemborders.neoforge.mixin;

import com.anthonyhilyard.itemborders.ItemBorders;

import com.anthonyhilyard.itemborders.config.ItemBordersConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin extends Screen
{
	protected AbstractContainerScreenMixin(Component titleIn) { super(titleIn); }

	// Renders under the item.
	@Inject(method = "renderSlot", at = @At("HEAD"))
	public void renderSlotBackground(GuiGraphics graphics, Slot slot, int i, int j, CallbackInfo ci)
	{
		if (!ItemBordersConfig.getInstance().overItems.get())
		{
			ItemBorders.renderBorder(graphics, slot);
			graphics.nextStratum();
		}
	}

	// Renders over the item.
	@Inject(method = "renderSlotContents", at = @At(value = "TAIL"), remap = false)
	public void renderSlotForeground(GuiGraphics graphics, ItemStack itemstack, Slot slot, String countString, CallbackInfo ci)
	{
		if (ItemBordersConfig.getInstance().overItems.get())
		{
			graphics.nextStratum();
			ItemBorders.renderBorder(graphics, slot);
		}
	}
}
