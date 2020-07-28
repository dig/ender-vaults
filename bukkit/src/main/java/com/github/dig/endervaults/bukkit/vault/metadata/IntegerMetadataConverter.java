package com.github.dig.endervaults.bukkit.vault.metadata;

import com.github.dig.endervaults.api.vault.metadata.MetadataConverter;

public class IntegerMetadataConverter implements MetadataConverter<Integer> {

    @Override
    public Integer to(String value) {
        return Integer.parseInt(value);
    }

    @Override
    public String from(Integer value) {
        return String.valueOf(value);
    }
}
