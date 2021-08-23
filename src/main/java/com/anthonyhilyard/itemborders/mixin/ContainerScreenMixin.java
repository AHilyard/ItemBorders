package com.anthonyhilyard.itemborders.mixin;

import com.anthonyhilyard.itemborders.ItemBorders;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;

@Mixin(ContainerScreen.class)
public class ContainerScreenMixin extends Screen
{
	protected ContainerScreenMixin(ITextComponent titleIn) { super(titleIn); }

	@Inject(method = "renderSlot(Lnet/minecraft/inventory/container/Slot;)V",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableDepthTest()V"))
	public void renderSlot(Slot slot, CallbackInfo info)
	{
		ItemBorders.renderBorder(slot);
	}
}
