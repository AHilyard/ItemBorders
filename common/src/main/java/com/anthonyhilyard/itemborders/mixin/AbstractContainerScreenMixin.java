package com.anthonyhilyard.itemborders.mixin;

import com.anthonyhilyard.itemborders.ItemBorders;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Group;
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

	@Group(name = "renderSlot")
	@Inject(method = "renderSlot", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", shift = Shift.AFTER))
	public void renderSlot(GuiGraphics graphics, Slot slot, CallbackInfo info)
	{
		ItemBorders.renderBorder(graphics.pose(), slot);
	}

	@Group(name = "renderSlot")
	@Inject(method = "renderSlotContents", at = @At(value = "TAIL"), remap = false)
	public void renderSlotContents(GuiGraphics graphics, ItemStack itemStack, Slot slot, String countString, CallbackInfo info)
	{
		ItemBorders.renderBorder(graphics.pose(), slot);
	}
}
