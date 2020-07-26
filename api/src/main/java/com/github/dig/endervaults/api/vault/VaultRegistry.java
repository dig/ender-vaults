package com.github.dig.endervaults.api.vault;

import java.util.List;
import java.util.UUID;

public interface VaultRegistry {

    Vault getVault(UUID ownerUUID, UUID id);

    List<Vault> getVaults(UUID ownerUUID);

}
