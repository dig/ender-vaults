package com.github.dig.endervaults.bukkit.vault.metadata;

import com.github.dig.endervaults.api.vault.metadata.MetadataConverter;

public class StringMetadataConverter implements MetadataConverter<String> {

    @Override
    public String to(String value) {
        return value;
    }

    @Override
    public String from(String value) {
        return value;
    }
}
