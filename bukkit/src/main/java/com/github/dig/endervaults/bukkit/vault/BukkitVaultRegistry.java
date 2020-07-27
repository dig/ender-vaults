package com.github.dig.endervaults.bukkit.vault;

import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.api.vault.VaultRegistry;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.javatuples.Pair;

import java.util.*;

public class BukkitVaultRegistry implements VaultRegistry {

    private final Table<UUID, UUID, Vault> vaults;
    public BukkitVaultRegistry() {
        this.vaults = HashBasedTable.create();
    }

    @Override
    public Optional<Vault> get(UUID ownerUUID, UUID id) {
        return Optional.ofNullable(vaults.get(ownerUUID, id));
    }

    @Override
    public Map<UUID, Vault> get(UUID ownerUUID) {
        return vaults.row(ownerUUID);
    }

    @Override
    public Optional<Vault> getByMetadata(UUID ownerUUID, String key, Object value) {
        return get(ownerUUID).values()
                .stream()
                .filter(vault -> vault.getMetadata().get(key) != null)
                .map(vault -> new Pair<>(vault, vault.getMetadata().get(key)))
                .filter(vaultObjectPair -> vaultObjectPair.getValue1().equals(value))
                .map(vaultObjectPair -> vaultObjectPair.getValue0())
                .findFirst();
    }

    @Override
    public Set<UUID> getAllOwners() {
        return vaults.rowKeySet();
    }

    @Override
    public synchronized void register(UUID ownerUUID, Vault vault) {
        vaults.put(ownerUUID, vault.getId(), vault);
    }

    @Override
    public synchronized void clean(UUID ownerUUID) {
        vaults.row(ownerUUID).clear();
    }
}
