package com.github.dig.endervaults.api.vault;

import java.util.UUID;

public interface VaultPersister {

    void load(UUID ownerUUID);

    void save(UUID ownerUUID);

    void save();

}
