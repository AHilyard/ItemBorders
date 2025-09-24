package com.anthonyhilyard.prism.text;

public class DynamicColor {
    private final int value;
    
    public DynamicColor(int value) {
        this.value = value;
    }
    
    public static DynamicColor fromRgb(int rgb) {
        return new DynamicColor(rgb);
    }
    
    public int getValue() {
        return value;
    }
}