package com.github.dig.endervaults.bukkit.storage;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.storage.DataStorage;
import com.github.dig.endervaults.api.util.VaultSerializable;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.api.vault.metadata.MetadataConverter;
import com.github.dig.endervaults.api.vault.metadata.VaultMetadataRegistry;
import com.github.dig.endervaults.bukkit.EVBukkitPlugin;
import com.github.dig.endervaults.bukkit.vault.BukkitVault;
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
import java.util.*;
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
        return get(ownerUUID);
    }

    @Override
    public Optional<Vault> load(UUID ownerUUID, UUID id) {
        return get(id, ownerUUID);
    }

    @Override
    public void save(Vault vault) {
        VaultMetadataRegistry metadataRegistry = plugin.getMetadataRegistry();
        if (exists(vault.getOwner(), vault.getId())) {
            update(vault.getId(), vault.getOwner(), vault.getSize(), ((VaultSerializable) vault).encode());
            for (String key : vault.getMetadata().keySet()) {
                Object value = vault.getMetadata().get(key);
                metadataRegistry.get(key)
                        .ifPresent(converter -> {
                            if (exists(vault.getId(), vault.getOwner(), key)) {
                                update(vault.getId(), vault.getOwner(), key, converter.from(value));
                            } else {
                                insert(vault.getId(), vault.getOwner(), key, converter.from(value));
                            }
                        });
            }
        } else {
            String contents = ((VaultSerializable) vault).encode();
            insert(vault.getId(), vault.getOwner(), vault.getSize(), contents);
            for (String key : vault.getMetadata().keySet()) {
                Object value = vault.getMetadata().get(key);
                metadataRegistry.get(key)
                        .ifPresent(converter -> insert(vault.getId(), vault.getOwner(), key, converter.from(value)));
            }
        }
    }

    private void createTableIfNotExist(String table, String TABLE_SQL) {
        TABLE_SQL = String.format(TABLE_SQL, table);
        try (Connection conn = hikariDataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(TABLE_SQL)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to create table " + table + ".", ex);
        }
    }

    private Vault create(UUID id, UUID ownerUUID, int size, String contents) {
        Map<String, Object> metadata = getVaultMetadata(ownerUUID, id);
        String title = plugin.getLanguage().get(Lang.VAULT_TITLE, metadata);
        BukkitVault vault = new BukkitVault(id, title, size, ownerUUID, metadata);

        VaultSerializable serializable = vault;
        serializable.decode(contents);
        return vault;
    }

    private Map<String, Object> getVaultMetadata(UUID ownerUUID, UUID id) {
        VaultMetadataRegistry metadataRegistry = plugin.getMetadataRegistry();

        Map<String, Object> metadata = new HashMap<>();
        String sql = String.format(DatabaseConstants.SQL_SELECT_VAULT_METADATA_BY_ID_AND_OWNER, metadataTable);
        try (Connection conn = hikariDataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            stmt.setString(2, ownerUUID.toString());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String key = rs.getString("name");
                Optional<MetadataConverter> converterOptional = metadataRegistry.get(key);
                if (converterOptional.isPresent()) {
                    MetadataConverter converter = converterOptional.get();
                    metadata.put(key, converter.to(rs.getString("value")));
                }
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "[EnderVaults] Error while executing query.", ex);
        }

        return metadata;
    }

    private void insert(UUID id, UUID ownerUUID, int size, String contents) {
        String sql = String.format(DatabaseConstants.SQL_INSERT_VAULT, vaultTable);
        try (Connection conn = hikariDataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            stmt.setString(2, ownerUUID.toString());
            stmt.setInt(3, size);
            stmt.setString(4, contents);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "[EnderVaults] Error while executing query.", ex);
        }
    }

    private void insert(UUID id, UUID ownerUUID, String key, String value) {
        String sql = String.format(DatabaseConstants.SQL_INSERT_VAULT_METADATA, metadataTable);
        try (Connection conn = hikariDataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            stmt.setString(2, ownerUUID.toString());
            stmt.setString(3, key);
            stmt.setString(4, value);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "[EnderVaults] Error while executing query.", ex);
        }
    }

    private void update(UUID id, UUID ownerUUID, int size, String contents) {
        String sql = String.format(DatabaseConstants.SQL_UPDATE_VAULT_BY_ID_AND_OWNER, vaultTable);
        try (Connection conn = hikariDataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, size);
            stmt.setString(2, contents);
            stmt.setString(3, id.toString());
            stmt.setString(4, ownerUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "[EnderVaults] Error while executing query.", ex);
        }
    }

    private void update(UUID id, UUID ownerUUID, String key, String value) {
        String sql = String.format(DatabaseConstants.SQL_UPDATE_VAULT_METADATA_BY_ID_AND_OWNER_AND_KEY, metadataTable);
        try (Connection conn = hikariDataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setString(2, id.toString());
            stmt.setString(3, ownerUUID.toString());
            stmt.setString(4, key);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "[EnderVaults] Error while executing query.", ex);
        }
    }

    private Optional<Vault> get(UUID id, UUID ownerUUID) {
        int size;
        String contents;
        String sql = String.format(DatabaseConstants.SQL_SELECT_VAULT_BY_ID_AND_OWNER, vaultTable);
        try (Connection conn = hikariDataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            stmt.setString(2, ownerUUID.toString());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                size = rs.getInt("size");
                contents = rs.getString("contents");
            } else {
                return Optional.empty();
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "[EnderVaults] Error while executing query.", ex);
            return Optional.empty();
        }
        return Optional.ofNullable(create(id, ownerUUID, size, contents));
    }

    private List<Vault> get(UUID ownerUUID) {
        List<Vault> vaults = new ArrayList<>();
        String sql = String.format(DatabaseConstants.SQL_SELECT_VAULT_BY_OWNER, vaultTable);
        try (Connection conn = hikariDataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ownerUUID.toString());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                int size = rs.getInt("size");
                String contents = rs.getString("contents");
                vaults.add(create(id, ownerUUID, size, contents));
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "[EnderVaults] Error while executing query.", ex);
        }
        return vaults;
    }

    private boolean exists(UUID id, UUID ownerUUID, String key) {
        String sql = String.format(DatabaseConstants.SQL_SELECT_VAULT_METADATA_BY_ID_AND_OWNER_AND_KEY, metadataTable);
        boolean has;
        try (Connection conn = hikariDataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            stmt.setString(2, ownerUUID.toString());
            stmt.setString(3, key);

            ResultSet rs = stmt.executeQuery();
            has = rs.next();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "[EnderVaults] Error while executing query.", ex);
            return false;
        }
        return has;
    }
}
