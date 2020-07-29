package com.github.dig.endervaults.api.storage;

import com.github.dig.endervaults.api.vault.Vault;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataStorage {

    boolean init(Storage storage);

    void close();

    boolean exists(UUID ownerUUID, UUID id);

    List<Vault> load(UUID ownerUUID);

    Optional<Vault> load(UUID ownerUUID, UUID id);

    void save(Vault vault) throws IOException;

}
