package com.anthonyhilyard.itemborders.fabric.mixin;

import com.anthonyhilyard.itemborders.ItemBorders;
import com.anthonyhilyard.itemborders.config.ItemBordersConfig;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin extends Screen
{
	protected AbstractContainerScreenMixin(Component titleIn) { super(titleIn); }

	// Renders under the item.
	@Inject(method = "extractSlot", at = @At("HEAD"))
	public void renderSlotBackground(GuiGraphicsExtractor graphics, Slot slot, int i, int j, CallbackInfo ci)
	{
		if (!ItemBordersConfig.getInstance().overItems.get())
		{
			ItemBorders.renderBorder(graphics, slot);
			graphics.nextStratum();
		}
	}

	// Renders over the item.
	@Inject(method = "extractSlot", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", shift = Shift.AFTER))
	public void renderSlotForeground(GuiGraphicsExtractor graphics, Slot slot, int i, int j, CallbackInfo ci)
	{
		if (ItemBordersConfig.getInstance().overItems.get())
		{
			graphics.nextStratum();
			ItemBorders.renderBorder(graphics, slot);
		}
	}
}