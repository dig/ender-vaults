package com.github.dig.endervaults.api.vault;

import java.util.UUID;

public interface VaultPersister {

    Vault load(UUID ownerUUID, UUID id);

    void save(Vault vault);

}
