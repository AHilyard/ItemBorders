package com.anthonyhilyard.iceberg.util;

import java.util.List;
import java.util.Collections;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

public class Selectors {
    
    public static class SelectorDocumentation {
        private final String name;
        private final String description;
        private final List<String> examples;
        
        public SelectorDocumentation(String name, String description, List<String> examples) {
            this.name = name;
            this.description = description;
            this.examples = examples;
        }
        
        public String name() { return name; }
        public String description() { return description; }
        public List<String> examples() { return examples; }
    }
    
    public static List<SelectorDocumentation> selectorDocumentation() {
        // Return empty list as stub
        return Collections.emptyList();
    }
    
    public static boolean itemMatches(ItemStack item, String selector, HolderLookup.Provider provider) {
        // Stub implementation - always return false
        return false;
    }
}