package com.github.dig.endervaults.api;

import com.github.dig.endervaults.api.exception.InvalidMinecraftVersionException;
import com.github.dig.endervaults.api.file.DataFile;
import com.github.dig.endervaults.api.nms.MinecraftVersion;
import com.github.dig.endervaults.api.nms.VaultNMS;
import com.github.dig.endervaults.nms.v1_16_R1.v1_16_R1NMS;

public interface EnderVaultsPlugin {

    MinecraftVersion getServerVersion();

    DataFile getLangFile();

    DataFile getConfigFile();

    default VaultNMS getNMSBridge() throws InvalidMinecraftVersionException {
        MinecraftVersion version = getServerVersion();
        switch (version) {
            case v1_16_R1:
                return new v1_16_R1NMS();
            default:
                throw new InvalidMinecraftVersionException("EnderVaults does not support " + version.toString() + "!");
        }
    }
}
