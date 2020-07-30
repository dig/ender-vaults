package com.github.dig.endervaults.bukkit.migration;

import com.github.dig.endervaults.api.VaultPluginProvider;
import com.github.dig.endervaults.api.migration.Migrator;
import com.github.dig.endervaults.api.storage.DataStorage;
import com.github.dig.endervaults.api.vault.metadata.VaultDefaultMetadata;
import com.github.dig.endervaults.bukkit.EVBukkitPlugin;
import com.github.dig.endervaults.bukkit.vault.BukkitVault;
import com.google.common.io.Files;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
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
public class PVXMigrator implements Migrator {

    private final EVBukkitPlugin plugin = (EVBukkitPlugin) VaultPluginProvider.getPlugin();
    private final DataStorage dataStorage = plugin.getDataStorage();

    @Override
    public boolean can() {
        File directory = getDirectory();
        return directory.exists() && directory.isDirectory();
    }

    @Override
    public String response() {
        return "Missing 'base64vaults' folder inside EnderVaults. Please move 'base64vaults' from PlayerVaultsX into EnderVaults.";
    }

    @Override
    public void migrate() {
        log.log(Level.INFO, "[EnderVaults] Starting migration from PlayerVaultsX.");
        long start = System.currentTimeMillis();
        int count = 0;

        File directory = getDirectory();
        File[] files = directory.listFiles((File f, String name) -> name.endsWith(".yml"));
        log.log(Level.INFO, "[EnderVaults] Found " + files.length + " player files with vaults.");

        for (File playerFile : files) {
            UUID ownerUUID = UUID.fromString(Files.getNameWithoutExtension(playerFile.getName()));
            if (ownerUUID != null) {
                FileConfiguration configuration = YamlConfiguration.loadConfiguration(playerFile);
                Set<String> vaultKeys = configuration.getKeys(false);
                log.log(Level.INFO, "[EnderVaults] Migrating " + vaultKeys.size() + " vaults for UUID " + ownerUUID.toString() + ".");

                for (String vaultName : vaultKeys) {
                    String vaultOrderStr = vaultName.replaceFirst("vault", "");
                    int order;
                    try {
                        order = Integer.parseInt(vaultOrderStr);
                    } catch (NumberFormatException e) {
                        log.log(Level.SEVERE, "[EnderVaults] Error while migrating vault, could not find vault order. (" + ownerUUID.toString() + ", " + vaultName + ")", e);
                        continue;
                    }

                    String contents = configuration.getString(vaultName)
                            .replace("\n", "")
                            .replace("\r", "");
                    Optional<Inventory> inventoryOptional = fromString(contents);
                    if (inventoryOptional.isPresent()) {
                        BukkitVault vault = new BukkitVault(UUID.randomUUID(), ownerUUID, inventoryOptional.get(), new HashMap<String, Object>(){{
                            put(VaultDefaultMetadata.ORDER.getKey(), order);
                        }});

                        try {
                            dataStorage.save(vault);
                            count++;
                        } catch (IOException e) {
                            log.log(Level.SEVERE, "[EnderVaults] Unable to save migrated vault. (" + ownerUUID.toString() + ", " + vaultName + ")", e);
                        }
                    }
                }
            }
        }

        if (directory.renameTo(new File(getDirectory().getParent(), "base64vaults-migrated"))) {
            log.log(Level.INFO, "[EnderVaults] Moved 'base64vaults' to 'base64vaults-migrated' due to successful migration. You may now delete 'base64vaults-migrated'.");
        }

        long finish = System.currentTimeMillis();
        long elapsed = finish - start;
        log.log(Level.INFO, String.format("[EnderVaults] Successfully migrated %d vaults from PlayerVaultsX. (took %d sec)", count, TimeUnit.MILLISECONDS.toSeconds(elapsed)));
    }

    private File getDirectory() {
        return new File(plugin.getDataFolder(), "base64vaults");
    }

    private Optional<Inventory> fromString(String contents) {
        byte[] decoded = Base64.getDecoder().decode(contents);
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decoded);
            BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream);
            int size = bukkitObjectInputStream.readInt();

            Inventory inventory = Bukkit.createInventory(null, size, "");
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, (ItemStack) bukkitObjectInputStream.readObject());
            }
            bukkitObjectInputStream.close();
            return Optional.ofNullable(inventory);
        } catch (IOException | ClassNotFoundException e) {
            log.log(Level.SEVERE, "[EnderVaults] Error while migrating vault, could not decode vault data.", e);
        }
        return Optional.empty();
    }
}
