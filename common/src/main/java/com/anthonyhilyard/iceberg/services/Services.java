package com.anthonyhilyard.iceberg.services;

// Stub class for Services
public class Services {
    public static PlatformHelper getPlatformHelper() {
        return new PlatformHelper();
    }
    
    public static class PlatformHelper {
        public boolean isModLoaded(String modId) {
            // Stub - return false for all mods
            return false;
        }
    }
}