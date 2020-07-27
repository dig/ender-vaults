package com.github.dig.endervaults.api.storage;

import com.github.dig.endervaults.api.vault.Vault;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataStorage {

    void init();

    void close();

    boolean exists(UUID ownerUUID, UUID id);

    List<UUID> getAll(UUID ownerUUID);

    Optional<Vault> load(UUID ownerUUID, UUID id);

    void save(Vault vault);

}
