package com.github.dig.endervaults.bukkit.storage;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.storage.DataStorage;
import com.github.dig.endervaults.api.storage.Storage;
import com.github.dig.endervaults.api.util.VaultSerializable;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.api.vault.metadata.VaultMetadataRegistry;
import com.github.dig.endervaults.bukkit.EVBukkitPlugin;
import com.github.dig.endervaults.bukkit.vault.BukkitVault;
import com.google.common.io.Files;
import lombok.extern.java.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

@Log
public class YamlStorage implements DataStorage {

    private final EVBukkitPlugin plugin = (EVBukkitPlugin) PluginProvider.getPlugin();

    @Override
    public boolean init(Storage storage) {
        return true;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean exists(UUID ownerUUID, UUID id) {
        return getVaultFile(ownerUUID, id).exists();
    }

    @Override
    public List<Vault> load(UUID ownerUUID) {
        List<Vault> vaults = new ArrayList<>();

        File file = getOwnerFolder(ownerUUID);
        if (file.isDirectory()) {
            File[] files = file.listFiles((File f, String name) -> name.endsWith(".yml"));
            for (File vaultFile : files) {
                UUID id = UUID.fromString(Files.getNameWithoutExtension(vaultFile.getName()));
                load(ownerUUID, id).ifPresent(vault -> vaults.add(vault));
            }
        }

        return vaults;
    }

    @Override
    public Optional<Vault> load(UUID ownerUUID, UUID id) {
        if (!exists(ownerUUID, id)) return Optional.empty();
        VaultMetadataRegistry metadataRegistry = plugin.getMetadataRegistry();
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(getVaultFile(ownerUUID, id));

        if (!configuration.contains("size") || !configuration.contains("contents") || !configuration.contains("metadata")) {
            return Optional.empty();
        }

        int size = configuration.getInt("size");

        ConfigurationSection metadataSection = configuration.getConfigurationSection("metadata");
        Map<String, Object> metadata = new HashMap<>();
        for (String key : metadataSection.getKeys(false)) {
            String value = metadataSection.getString(key);
            metadataRegistry.get(key).ifPresent(converter -> metadata.put(key, converter.to(value)));
        }

        String title = plugin.getLanguage().get(Lang.VAULT_TITLE, metadata);
        BukkitVault vault = new BukkitVault(id, title, size, ownerUUID, metadata);

        VaultSerializable serializable = vault;
        serializable.decode(configuration.getString("contents"));

        return Optional.ofNullable(vault);
    }

    @Override
    public void save(Vault vault) throws IOException {
        File file = getVaultFile(vault.getOwner(), vault.getId());
        file.getParentFile().mkdirs();
        file.createNewFile();

        VaultMetadataRegistry metadataRegistry = plugin.getMetadataRegistry();
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        configuration.set("size", vault.getSize());
        for (String key : vault.getMetadata().keySet()) {
            Object value = vault.getMetadata().get(key);
            metadataRegistry.get(key).ifPresent(converter -> configuration.set("metadata." + key, converter.from(value)));
        }

        VaultSerializable serializable = (VaultSerializable) vault;
        configuration.set("contents", serializable.encode());

        configuration.save(file);
    }

    private String getDirectoryName() {
        FileConfiguration configuration = (FileConfiguration) plugin.getConfigFile().getConfiguration();
        return configuration.getString("storage.settings.flatfile.directory", "data");
    }

    private File getOwnerFolder(UUID ownerUUID) {
        String filePath = getDirectoryName() + File.separator + ownerUUID.toString();
        return new File(plugin.getDataFolder(), filePath);
    }

    private File getVaultFile(UUID ownerUUID, UUID id) {
        String filePath = getDirectoryName() + File.separator + ownerUUID.toString() + File.separator + id.toString() + ".yml";
        return new File(plugin.getDataFolder(), filePath);
    }
}
