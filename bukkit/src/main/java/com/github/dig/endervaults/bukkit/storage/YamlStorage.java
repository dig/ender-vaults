package com.github.dig.endervaults.bukkit.storage;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.file.DataFile;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.storage.DataStorage;
import com.github.dig.endervaults.api.util.VaultSerializable;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.api.vault.metadata.VaultMetadataRegistry;
import com.github.dig.endervaults.bukkit.EVBukkitPlugin;
import com.github.dig.endervaults.bukkit.file.BukkitDataFile;
import com.github.dig.endervaults.bukkit.vault.BukkitVault;
import lombok.extern.java.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

@Log
public class YamlStorage implements DataStorage {

    private final EVBukkitPlugin plugin = (EVBukkitPlugin) PluginProvider.getPlugin();

    @Override
    public void init() {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean exists(UUID ownerUUID, UUID id) {
        return getVaultFile(ownerUUID, id).exists();
    }

    @Override
    public List<UUID> getAll(UUID ownerUUID) {
        List<UUID> vaults = new ArrayList<>();

        File file = getOwnerFolder(ownerUUID);
        if (file.isDirectory()) {

        }

        return vaults;
    }

    @Override
    public Optional<Vault> load(UUID ownerUUID, UUID id) {
        if (!exists(ownerUUID, id)) return Optional.empty();
        DataFile dataFile = new BukkitDataFile(getVaultFile(ownerUUID, id));
        FileConfiguration configuration = (FileConfiguration) dataFile.getConfiguration();

        int size = configuration.getInt("size");

        VaultMetadataRegistry metadataRegistry = plugin.getMetadataRegistry();
        ConfigurationSection metadataSection = configuration.getConfigurationSection("metadata");

        Map<String, Object> metadata = new HashMap<>();
        for (String key : metadataSection.getKeys(false)) {
            Object value = metadataSection.get(key);
            metadataRegistry.get(key).ifPresent(converter -> metadata.put(key, converter.from(value)));
        }

        String title = plugin.getLanguage().get(Lang.VAULT_TITLE, metadata);
        BukkitVault vault = new BukkitVault(id, title, size, ownerUUID, metadata);

        VaultSerializable serializable = vault;
        serializable.decode(configuration.getString("contents"));

        return Optional.ofNullable(vault);
    }

    @Override
    public void save(Vault vault) {
        File file = getVaultFile(vault.getOwner(), vault.getId());
        file.getParentFile().mkdirs();

        try {
            file.createNewFile();
        } catch (IOException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to create file for vault.", e);
            return;
        }

        DataFile dataFile = new BukkitDataFile(file);
        FileConfiguration configuration = (FileConfiguration) dataFile.getConfiguration();

        configuration.set("size", vault.getSize());
        for (String key : vault.getMetadata().keySet()) {
            Object value = vault.getMetadata().get(key);
            configuration.set("metadata." + key, value);
        }

        VaultSerializable serializable = (VaultSerializable) vault;
        configuration.set("contents", serializable.encode());

        dataFile.save();
    }

    private File getOwnerFolder(UUID ownerUUID) {
        String filePath = "data" + File.separator + ownerUUID.toString();
        return new File(plugin.getDataFolder(), filePath);
    }

    private File getVaultFile(UUID ownerUUID, UUID id) {
        String filePath = "data" + File.separator + ownerUUID.toString() + File.separator + id.toString() + ".yml";
        return new File(plugin.getDataFolder(), filePath);
    }
}
