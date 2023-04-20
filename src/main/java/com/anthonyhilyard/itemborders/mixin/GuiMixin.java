package com.anthonyhilyard.itemborders.mixin;

import com.anthonyhilyard.itemborders.ItemBorders;
import com.mojang.blaze3d.vertex.PoseStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Gui.class)
public class GuiMixin
{
	@Inject(method = "renderSlot",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderAndDecorateItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V", shift = Shift.AFTER))
	public void renderSlot(PoseStack poseStack, int x, int y, float time, Player player, ItemStack item, int something, CallbackInfo info)
	{
		ItemBorders.renderBorder(poseStack, item, x, y);
	}
}
