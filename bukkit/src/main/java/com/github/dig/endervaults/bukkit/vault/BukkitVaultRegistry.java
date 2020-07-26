package com.github.dig.endervaults.bukkit.vault;

import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.api.vault.VaultRegistry;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BukkitVaultRegistry implements VaultRegistry {

    private Table<UUID, UUID, Vault> vaults;
    public BukkitVaultRegistry() {
        this.vaults = HashBasedTable.create();
    }

    @Override
    public Vault get(UUID ownerUUID, UUID id) {
        return vaults.get(ownerUUID, id);
    }

    @Override
    public Map<UUID, Vault> get(UUID ownerUUID) {
        return vaults.row(ownerUUID);
    }

    @Override
    public UUID register(UUID ownerUUID, Vault vault) {
        UUID id = UUID.randomUUID();
        vaults.put(ownerUUID, id, vault);
        return id;
    }
}
