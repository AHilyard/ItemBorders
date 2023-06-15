package com.anthonyhilyard.itemborders.mixin;

import com.anthonyhilyard.itemborders.ItemBorders;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

@Mixin(GuiIngame.class)
public class GuiIngameMixin
{
	@Inject(method = "func_184044_a", remap = false, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/RenderItem;(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;II)V", shift = Shift.AFTER))
	public void renderHotbarItemItemBorders(int x, int y, float time, EntityPlayer player, ItemStack item, CallbackInfo info)
	{
		ItemBorders.renderBorder(item, x, y);
	}
}
