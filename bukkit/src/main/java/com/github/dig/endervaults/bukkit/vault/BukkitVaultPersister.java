package com.github.dig.endervaults.bukkit.vault;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.storage.DataStorage;
import com.github.dig.endervaults.api.vault.VaultPersister;
import com.github.dig.endervaults.api.vault.VaultRegistry;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

@Log
public class BukkitVaultPersister implements VaultPersister {

    private final DataStorage dataStorage = PluginProvider.getPlugin().getDataStorage();
    private final VaultRegistry registry = PluginProvider.getPlugin().getRegistry();
    private final List<UUID> persisted = new ArrayList<>();

    @Override
    public void load(UUID ownerUUID) {
        registry.clean(ownerUUID);
        dataStorage.load(ownerUUID).forEach(vault -> registry.register(ownerUUID, vault));
        persisted.add(ownerUUID);
    }

    @Override
    public void save(UUID ownerUUID) {
        registry.get(ownerUUID).values().forEach(vault -> {
            try {
                dataStorage.save(vault);
            } catch (IOException e) {
                log.log(Level.SEVERE,
                        "[EnderVaults] Unable to save vault " + vault.getId() + " for player " + ownerUUID + ".", e);
            }
        });

        persisted.remove(ownerUUID);
        registry.clean(ownerUUID);
    }

    @Override
    public void save() {
        Bukkit.getOnlinePlayers()
                .stream()
                .map(player -> player.getUniqueId())
                .forEach(this::save);
    }

    @Override
    public boolean isLoaded(UUID ownerUUID) {
        return persisted.contains(ownerUUID);
    }
}
