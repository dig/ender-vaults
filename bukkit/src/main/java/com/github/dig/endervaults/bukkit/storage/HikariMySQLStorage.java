package com.github.dig.endervaults.bukkit.storage;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.storage.DataStorage;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.bukkit.EVBukkitPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HikariMySQLStorage implements DataStorage {

    private final EVBukkitPlugin plugin = (EVBukkitPlugin) PluginProvider.getPlugin();
    private HikariDataSource hikariDataSource;

    @Override
    public void init() {
        FileConfiguration config = (FileConfiguration) plugin.getConfigFile().getConfiguration();
        ConfigurationSection settings = config.getConfigurationSection("storage.settings.mysql");

        String address = settings.getString("address", "localhost");
        String database = settings.getString("database", "minecraft");
        String user = settings.getString("user", "minecraft");
        String password = settings.getString("password", "123");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s/%s", address, database));

        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);

        ConfigurationSection properties = settings.getConfigurationSection("properties");
        for (String key : properties.getKeys(false)) {
            hikariConfig.addDataSourceProperty(key, properties.getString(key));
        }
        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void close() {
        if (hikariDataSource.isRunning()) {
            hikariDataSource.close();
        }
    }

    @Override
    public boolean exists(UUID ownerUUID, UUID id) {
        return false;
    }

    @Override
    public List<Vault> load(UUID ownerUUID) {
        return null;
    }

    @Override
    public Optional<Vault> load(UUID ownerUUID, UUID id) {
        return Optional.empty();
    }

    @Override
    public void save(Vault vault) {

    }
}
