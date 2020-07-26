package com.github.dig.endervaults.bukkit.file;

import com.github.dig.endervaults.api.file.DataFile;
import lombok.extern.java.Log;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
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
            configuration = YamlConfiguration.loadConfiguration(file);
        } catch (IllegalArgumentException e) {
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
