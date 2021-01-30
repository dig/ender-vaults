package com.github.dig.endervaults.bukkit.migration;

import com.github.dig.endervaults.api.VaultPluginProvider;
import com.github.dig.endervaults.api.migration.Migrator;
import com.github.dig.endervaults.api.storage.DataStorage;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.api.vault.metadata.VaultDefaultMetadata;
import com.github.dig.endervaults.bukkit.EVBukkitPlugin;
import com.github.dig.endervaults.bukkit.vault.BukkitVault;
import com.github.dig.endervaults.bukkit.vault.BukkitVaultFactory;
import com.google.common.io.Files;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Log
public class ECMigrator implements Migrator {

    private final EVBukkitPlugin plugin = (EVBukkitPlugin) VaultPluginProvider.getPlugin();
    private final DataStorage dataStorage = plugin.getDataStorage();

    @Override
    public boolean can() {
        return getDirectory().exists() && getDirectory().isDirectory();
    }

    @Override
    public String response() {
        return "Missing 'ecdata' folder inside EnderVaults. Please move 'data' and rename to 'ecdata' from EnderContainers into EnderVaults.";
    }

    @Override
    public void migrate() {
        log.log(Level.INFO, "[EnderVaults] Starting migration from EnderContainers.");
        long start = System.currentTimeMillis();
        int count = 0;

        File directory = getDirectory();
        File[] files = directory.listFiles((File f, String name) -> name.endsWith(".yml"));
        log.log(Level.INFO, "[EnderVaults] Found " + files.length + " player files with vaults.");

        FileConfiguration config = (FileConfiguration) plugin.getConfigFile().getConfiguration();
        int defaultSize = config.getInt("vault.default-rows", 3) * 9;

        for (File playerFile : files) {
            UUID ownerUUID;
            try {
                String uuid = Files.getNameWithoutExtension(playerFile.getName())
                        .replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                "$1-$2-$3-$4-$5");
                ownerUUID = UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                log.log(Level.SEVERE, "[EnderVaults] Skipping vault, not a valid UUID. (" + playerFile.getName() + ")", e);
                continue;
            }

            if (ownerUUID != null) {
                FileConfiguration configuration = YamlConfiguration.loadConfiguration(playerFile);
                ConfigurationSection vaults = configuration.getConfigurationSection("enderchests");
                Set<String> vaultKeys = vaults.getKeys(false);
                log.log(Level.INFO, "[EnderVaults] Migrating " + vaultKeys.size() + " vaults for UUID " + ownerUUID.toString() + ".");

                for (String vaultName : vaultKeys) {
                    int order;
                    try {
                        order = Integer.parseInt(vaultName);
                    } catch (NumberFormatException e) {
                        log.log(Level.SEVERE, "[EnderVaults] Skipping vault, could not find order. (" + ownerUUID.toString() + ", " + vaultName + ")", e);
                        continue;
                    }

                    if (vaults.getConfigurationSection(vaultName).contains("contents")) {
                        String contents = vaults.getConfigurationSection(vaultName).getString("contents");

                        Map<Integer, ItemStack> items;
                        try {
                            items = deserialize(contents);
                        } catch (IOException e) {
                            log.log(Level.SEVERE, "[EnderVaults] Unable to deserialize vault " + vaultName + " for UUID " + ownerUUID.toString() + ", skipping...", e);
                            continue;
                        }

                        int newSize = defaultSize;
                        for (int pos : items.keySet()) {
                            if (pos >= newSize) {
                                newSize = 54;
                            }
                        }

                        Inventory inventory = Bukkit.createInventory(null, newSize, "");
                        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                            if (entry.getKey() < newSize) {
                                inventory.setItem(entry.getKey(), entry.getValue());
                            }
                        }

                        BukkitVault vault = new BukkitVault(UUID.randomUUID(), ownerUUID, inventory, new HashMap<String, Object>(){{
                            put(VaultDefaultMetadata.ORDER.getKey(), order);
                        }});

                        try {
                            dataStorage.save(vault);
                            count++;
                        } catch (IOException e) {
                            log.log(Level.SEVERE, "[EnderVaults] Unable to save migrated vault. (" + ownerUUID.toString() + ", " + vaultName + ")", e);
                        }
                    } else {
                        log.log(Level.INFO, "[EnderVaults] Skipping vault " + vaultName + " for UUID " + ownerUUID.toString() + " due to no contents.");
                    }
                }
            }
        }

        if (directory.renameTo(new File(getDirectory().getParent(), "ecdata-migrated"))) {
            log.log(Level.INFO, "[EnderVaults] Moved 'ecdata' to 'ecdata-migrated' due to successful migration. You may now delete 'ecdata-migrated'.");
        }

        long finish = System.currentTimeMillis();
        long elapsed = finish - start;
        log.log(Level.INFO, String.format("[EnderVaults] Successfully migrated %d vaults from EnderContainers. (took %d sec)", count, TimeUnit.MILLISECONDS.toSeconds(elapsed)));
    }

    private File getDirectory() {
        return new File(plugin.getDataFolder(), "ecdata");
    }

    private Map<Integer, ItemStack> deserialize(String data) throws IOException {
        Map<Integer, ItemStack> items = new HashMap<>();

        byte[] bytes = Base64.getMimeDecoder().decode(data);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

        int mapSize = dataInput.readInt();
        int pos;
        ItemStack item;

        for (int i = 0; i < mapSize; i++) {
            pos = dataInput.readInt();
            try {
                item = (ItemStack) dataInput.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException("cannot found ItemStack class during deserialization", e);
            }

            items.put(pos, item);
        }

        dataInput.close();
        return items;
    }
}
