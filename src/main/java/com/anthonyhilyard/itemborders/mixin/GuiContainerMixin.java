package com.anthonyhilyard.itemborders.mixin;

import com.anthonyhilyard.itemborders.ItemBorders;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

@Mixin(GuiContainer.class)
public class GuiContainerMixin
{
	@Inject(method = "func_146977_a", remap = false, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/RenderItem;(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;II)V", shift = Shift.AFTER))
	public void drawSlotItemBorders(Slot slot, CallbackInfo info)
	{
		ItemBorders.renderBorder(slot);
	}
}
