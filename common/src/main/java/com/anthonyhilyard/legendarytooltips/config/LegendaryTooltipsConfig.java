package com.anthonyhilyard.legendarytooltips.config;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup;
import java.util.function.Supplier;

public class LegendaryTooltipsConfig {
    
    public static class FrameDefinition {
        private final int index;
        private final Supplier<Integer> startBorder;
        private final Supplier<Integer> endBorder;
        
        public FrameDefinition(int index, Supplier<Integer> startBorder, Supplier<Integer> endBorder) {
            this.index = index;
            this.startBorder = startBorder;
            this.endBorder = endBorder;
        }
        
        public int index() { return index; }
        public Supplier<Integer> startBorder() { return startBorder; }
        public Supplier<Integer> endBorder() { return endBorder; }
    }
    
    private static LegendaryTooltipsConfig instance = new LegendaryTooltipsConfig();
    
    public static LegendaryTooltipsConfig getInstance() {
        return instance;
    }
    
    public FrameDefinition getFrameDefinition(ItemStack item, HolderLookup.Provider registryAccess) {
        // Stub implementation - return frame with index -1 to indicate no frame
        return new FrameDefinition(-1, () -> 0xFFFFFF, () -> 0xFFFFFF);
    }
}