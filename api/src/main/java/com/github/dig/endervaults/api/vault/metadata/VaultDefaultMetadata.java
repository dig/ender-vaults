package com.github.dig.endervaults.api.vault.metadata;

import lombok.Getter;

public enum VaultDefaultMetadata {

    ORDER("order");

    @Getter
    private String key;

    VaultDefaultMetadata(String key) {
        this.key = key;
    }
}
