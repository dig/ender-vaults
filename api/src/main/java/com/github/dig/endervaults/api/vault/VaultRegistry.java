package com.github.dig.endervaults.api.vault;

import java.util.Map;
import java.util.UUID;

public interface VaultRegistry {

    Vault get(UUID ownerUUID, UUID id);

    Map<UUID, Vault> get(UUID ownerUUID);

    UUID register(UUID ownerUUID, Vault vault);
}
