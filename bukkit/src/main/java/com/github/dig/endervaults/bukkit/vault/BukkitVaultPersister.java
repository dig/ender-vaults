package com.github.dig.endervaults.bukkit.vault;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.storage.DataStorage;
import com.github.dig.endervaults.api.vault.VaultPersister;

import java.util.UUID;

public class BukkitVaultPersister implements VaultPersister {

    private final DataStorage dataStorage = PluginProvider.getPlugin().getDataStorage();

    @Override
    public void load(UUID ownerUUID) {

    }

    @Override
    public void save(UUID ownerUUID) {

    }
}
