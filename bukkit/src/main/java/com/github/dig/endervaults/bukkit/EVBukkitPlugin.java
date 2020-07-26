package com.github.dig.endervaults.bukkit;

import com.github.dig.endervaults.api.EnderVaultsPlugin;
import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.exception.PluginAlreadySetException;
import com.github.dig.endervaults.api.file.DataFile;
import com.github.dig.endervaults.api.lang.Language;
import com.github.dig.endervaults.nms.InvalidMinecraftVersionException;
import com.github.dig.endervaults.nms.MinecraftVersion;
import com.github.dig.endervaults.api.vault.VaultPersister;
import com.github.dig.endervaults.api.vault.VaultRegistry;
import com.github.dig.endervaults.bukkit.command.VaultCommand;
import com.github.dig.endervaults.bukkit.file.BukkitDataFile;
import com.github.dig.endervaults.bukkit.lang.BukkitLanguage;
import com.github.dig.endervaults.bukkit.vault.BukkitVaultPersister;
import com.github.dig.endervaults.bukkit.vault.BukkitVaultRegistry;
import com.github.dig.endervaults.nms.NMSProvider;
import com.github.dig.endervaults.nms.VaultNMS;
import com.github.dig.endervaults.nms.v1_16_R1.v1_16_R1NMS;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

@Log
public class EVBukkitPlugin extends JavaPlugin implements EnderVaultsPlugin {

    private DataFile langFile;
    private DataFile configFile;

    private VaultRegistry registry;
    private VaultPersister persister;
    private Language language;

    @Override
    public MinecraftVersion getVersion() {
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
    public VaultRegistry getRegistry() {
        return registry;
    }

    @Override
    public VaultPersister getPersister() {
        return persister;
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    @Override
    public void onEnable() {
        if (!setProviders()) return;
        loadConfiguration();
        setupManagers();
        registerCommands();
    }

    private boolean setProviders() {
        try {
            PluginProvider.set(this);
            setNMSProvider();
            return true;
        } catch (PluginAlreadySetException | InvalidMinecraftVersionException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to set providers, disabling...", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
        return false;
    }

    private void setNMSProvider() throws InvalidMinecraftVersionException {
        MinecraftVersion version = getVersion();
        switch (version) {
            case v1_16_R1:
                NMSProvider.set(new v1_16_R1NMS(), version);
                break;
            default:
                throw new InvalidMinecraftVersionException("Version of minecraft not supported.");
        }
    }

    private void loadConfiguration() {
        langFile = new BukkitDataFile(new File(getDataFolder(), "lang.yml"));
        configFile = new BukkitDataFile(new File(getDataFolder(), "config.yml"));
    }

    private void setupManagers() {
        registry = new BukkitVaultRegistry();
        persister = new BukkitVaultPersister();
        language = new BukkitLanguage();
    }

    private void registerCommands() {
        getCommand("vault").setExecutor(new VaultCommand());
    }
}
