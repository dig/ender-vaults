package com.github.dig.endervaults.bukkit;

import com.github.dig.endervaults.api.EnderVaultsPlugin;
import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.exception.PluginAlreadySetException;
import com.github.dig.endervaults.api.file.DataFile;
import com.github.dig.endervaults.api.nms.MinecraftVersion;
import com.github.dig.endervaults.bukkit.file.BukkitDataFile;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

@Log
public class EVBukkitPlugin extends JavaPlugin implements EnderVaultsPlugin {

    private DataFile langFile;
    private DataFile configFile;

    @Override
    public MinecraftVersion getServerVersion() {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        MinecraftVersion nmsVersion = MinecraftVersion.valueOf(version.substring(version.lastIndexOf('.') + 1));
        if (nmsVersion != null) return nmsVersion;
        return null;
    }

    @Override
    public DataFile getLangFile() {
        return langFile;
    }

    @Override
    public DataFile getConfigFile() {
        return configFile;
    }

    @Override
    public void onEnable() {
        try {
            PluginProvider.set(this);
        } catch (PluginAlreadySetException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to set plugin instance.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        langFile = new BukkitDataFile(new File(getDataFolder(), "lang.yml"));
        configFile = new BukkitDataFile(new File(getDataFolder(), "config.yml"));
    }
}
