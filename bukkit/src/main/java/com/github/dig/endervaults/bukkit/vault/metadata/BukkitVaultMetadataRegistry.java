package com.github.dig.endervaults.bukkit.vault.metadata;

import com.github.dig.endervaults.api.vault.metadata.MetadataConverter;
import com.github.dig.endervaults.api.vault.metadata.VaultMetadataRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BukkitVaultMetadataRegistry implements VaultMetadataRegistry {

    private final Map<String, MetadataConverter> registered;
    public BukkitVaultMetadataRegistry() {
        registered = new HashMap<>();
    }

    @Override
    public void register(String key, MetadataConverter converter) {
        registered.put(key, converter);
    }

    @Override
    public Optional<MetadataConverter> get(String key) {
        return Optional.ofNullable(registered.get(key));
    }
}
