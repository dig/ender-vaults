package com.github.dig.endervaults.bukkit.vault;

import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.api.vault.VaultPersister;

import java.util.UUID;

public class BukkitVaultPersister implements VaultPersister {

    @Override
    public Vault load(UUID ownerUUID, UUID id) {
        return null;
    }

    @Override
    public void save(Vault vault) {
    }
}
