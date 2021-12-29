package com.anthonyhilyard.itemborders.mixin;

import com.anthonyhilyard.itemborders.ItemBorders;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Mixin(IngameGui.class)
public class IngameGuiMixin extends AbstractGui
{
	@Inject(method = "renderSlot(IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;renderAndDecorateItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;II)V", shift = Shift.AFTER))
	public void renderSlot(int x, int y, float time, PlayerEntity player, ItemStack item, CallbackInfo info)
	{
		ItemBorders.renderBorder(item, x, y);
	}
}
