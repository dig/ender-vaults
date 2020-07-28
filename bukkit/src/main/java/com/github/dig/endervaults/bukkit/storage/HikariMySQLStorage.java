package com.github.dig.endervaults.bukkit.storage;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.storage.DataStorage;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.bukkit.EVBukkitPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import lombok.extern.java.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

@Log
public class HikariMySQLStorage implements DataStorage {

    private final EVBukkitPlugin plugin = (EVBukkitPlugin) PluginProvider.getPlugin();

    private HikariDataSource hikariDataSource;
    private String vaultTable;
    private String metadataTable;

    @Override
    public boolean init() {
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

        try {
            hikariDataSource = new HikariDataSource(hikariConfig);
        } catch (HikariPool.PoolInitializationException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to connect to database.", e);
            return false;
        }

        vaultTable = settings.getString("tables.vault");
        metadataTable = settings.getString("tables.vault-metadata");

        createTableIfNotExist(vaultTable, DatabaseConstants.SQL_CREATE_TABLE_VAULT);
        createTableIfNotExist(metadataTable, DatabaseConstants.SQL_CREATE_TABLE_VAULT_METADATA);
        return hikariDataSource.isRunning();
    }

    @Override
    public void close() {
        if (hikariDataSource != null && hikariDataSource.isRunning()) {
            hikariDataSource.close();
        }
    }

    @Override
    public boolean exists(UUID ownerUUID, UUID id) {
        String sql = String.format(DatabaseConstants.SQL_SELECT_VAULT_BY_ID_AND_OWNER, vaultTable);

        boolean has;
        try (Connection conn = hikariDataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            stmt.setString(2, ownerUUID.toString());

            ResultSet rs = stmt.executeQuery();
            has = rs.next();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "[EnderVaults] Error while executing query.", ex);
            return false;
        }

        return has;
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

    private void createTableIfNotExist(String table, String TABLE_SQL) {
        TABLE_SQL = String.format(TABLE_SQL, table);
        try (Connection conn = hikariDataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(TABLE_SQL)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to create table " + table + ".", ex);
        }
    }
}
