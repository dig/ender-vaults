package com.github.dig.endervaults.api.vault;

import java.util.UUID;

public interface VaultPersister {

    boolean exists(UUID ownerUUID, UUID id);

    Vault load(UUID ownerUUID, UUID id);

    void save(Vault vault);

}
