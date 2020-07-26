package com.github.dig.endervaults.nms;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NMSProvider {

    @Getter
    private static VaultNMS bridge = null;
    @Getter
    private static MinecraftVersion version = null;

    public static void set(VaultNMS instance, MinecraftVersion ver) {
        if (bridge == null) {
            bridge = instance;
            version = ver;
        }
    }
}

