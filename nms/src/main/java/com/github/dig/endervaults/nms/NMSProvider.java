package com.github.dig.endervaults.nms;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
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

            instance.init(ver);
            log.log(Level.INFO, "[EnderVaults] Found version: " + version.toString() + ", supported!");
        }
    }
}

