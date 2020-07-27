package com.github.dig.endervaults.bukkit.vault;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.storage.DataStorage;
import com.github.dig.endervaults.api.vault.VaultRegistry;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.logging.Level;

@Log
public class BukkitVaultAutoSave implements Runnable {

    private final VaultRegistry registry = PluginProvider.getPlugin().getRegistry();
    private final DataStorage dataStorage = PluginProvider.getPlugin().getDataStorage();

    @Override
    public void run() {
        log.log(Level.INFO, "[EnderVaults] Starting auto save of all registered vaults.");

        registry.getAllOwners().forEach(ownerUUID ->
            registry.get(ownerUUID).values().forEach(vault -> {
                try {
                    dataStorage.save(vault);
                } catch (IOException e) {
                    log.log(Level.SEVERE,
                            "[EnderVaults] Unable to save vault " + vault.getId() + " for player " + ownerUUID + ".", e);
                }
            })
        );

        log.log(Level.INFO, "[EnderVaults] Successfully saved all registered vaults.");
    }
}
