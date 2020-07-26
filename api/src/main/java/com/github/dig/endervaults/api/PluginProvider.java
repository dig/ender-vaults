package com.github.dig.endervaults.api;

import com.github.dig.endervaults.api.exception.PluginAlreadySetException;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PluginProvider {

    @Getter
    private static EnderVaultsPlugin plugin = null;

    public void set(EnderVaultsPlugin instance) throws PluginAlreadySetException {
        if (plugin == null) {
            plugin = instance;
            return;
        }
        throw new PluginAlreadySetException("Plugin instance already set.");
    }
}
