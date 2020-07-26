package com.github.dig.endervaults.api.vault;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface VaultRegistry {

    Optional<Vault> get(UUID ownerUUID, UUID id);

    Map<UUID, Vault> get(UUID ownerUUID);

    Optional<Vault> getByMetadata(UUID ownerUUID, String key, Object value);

    void register(UUID ownerUUID, Vault vault);
}
