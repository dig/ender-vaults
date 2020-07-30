package com.github.dig.endervaults.bukkit.file;

import com.github.dig.endervaults.api.VaultPluginProvider;
import com.github.dig.endervaults.api.file.DataFile;
import com.github.dig.endervaults.bukkit.EVBukkitPlugin;
import com.google.common.io.ByteStreams;
import lombok.extern.java.Log;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.logging.Level;

@Log
public class BukkitDataFile implements DataFile<FileConfiguration> {

    private final File file;
    private FileConfiguration configuration;

    public BukkitDataFile(File file) {
        this.file = file;
        this.load();
    }

    @Override
    public FileConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void load() {
        try {
            if (!file.exists()) {
                EVBukkitPlugin plugin = (EVBukkitPlugin) VaultPluginProvider.getPlugin();
                plugin.getDataFolder().mkdirs();

                try (InputStream is = plugin.getResource(file.getName());
                     OutputStream os = new FileOutputStream(file)) {
                    ByteStreams.copy(is, os);
                }
            }
            configuration = YamlConfiguration.loadConfiguration(file);
        } catch (IllegalArgumentException | IOException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to read file " + file.getName() + "!", e);
        }
    }

    @Override
    public void save() {
        try {
            configuration.save(file);
        } catch (IOException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to save file " + file.getName() + "!", e);
        }
    }
}
