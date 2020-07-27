package com.github.dig.endervaults.bukkit.vault.metadata;

import com.github.dig.endervaults.api.vault.metadata.MetadataConverter;

public class IntegerMetadataConverter implements MetadataConverter<Integer> {

    @Override
    public Integer to(Object value) {
        return Integer.parseInt(String.valueOf(value));
    }

    @Override
    public Object from(Integer value) {
        return value;
    }
}
