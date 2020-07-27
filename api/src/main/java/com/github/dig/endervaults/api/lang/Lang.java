package com.github.dig.endervaults.api.lang;

import lombok.Getter;

public enum Lang {

    VAULT_SELECTOR_TITLE("vault-selector-title"),
    VAULT_TITLE("vault-title"),
    INVALID_VAULT_ORDER("invalid-vault-order"),
    PLAYER_NOT_LOADED("player-not-loaded"),
    NO_PERMISSION("no-permission"),
    CONFIG_RELOAD("config-reload");

    @Getter
    private String key;

    Lang(String key) {
        this.key = key;
    }
}
