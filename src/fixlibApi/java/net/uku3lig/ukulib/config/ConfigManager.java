package net.uku3lig.ukulib.config;

import java.io.Serializable;

/**
 * Compile-only API bridge matching the FixLib ConfigManager methods used by
 * JoinBell. This source set is not included in the published JoinBell JAR.
 */
public class ConfigManager<T extends Serializable> {
    public static <T extends Serializable> ConfigManager<T> createDefault(Class<T> configClass, String name) {
        throw new UnsupportedOperationException("Compile-only FixLib API bridge");
    }

    public T getConfig() {
        throw new UnsupportedOperationException("Compile-only FixLib API bridge");
    }

    public void saveConfig() {
        throw new UnsupportedOperationException("Compile-only FixLib API bridge");
    }
}
