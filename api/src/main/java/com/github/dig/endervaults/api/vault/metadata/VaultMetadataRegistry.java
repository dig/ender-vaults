package com.github.dig.endervaults.api.vault.metadata;

import java.util.Optional;

public interface VaultMetadataRegistry {

    void register(String key, MetadataConverter converter);

    Optional<MetadataConverter> get(String key);

}
