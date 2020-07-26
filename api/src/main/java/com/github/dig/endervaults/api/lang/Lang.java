package com.github.dig.endervaults.api.lang;

import lombok.Getter;

public enum Lang {

    VAULT_SELECTOR_TITLE("vault-selector-title"),
    VAULT_TITLE("vault-title");

    @Getter
    private String key;

    Lang(String key) {
        this.key = key;
    }
}
