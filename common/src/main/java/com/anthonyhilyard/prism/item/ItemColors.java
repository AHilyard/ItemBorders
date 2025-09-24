package com.anthonyhilyard.prism.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.TextColor;

public class ItemColors {
    public static TextColor getColorForItem(ItemStack item, Object context) {
        // Stub implementation - return white for any item
        return TextColor.fromRgb(0xFFFFFF);
    }
}