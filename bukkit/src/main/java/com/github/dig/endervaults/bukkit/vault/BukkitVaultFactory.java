package com.github.dig.endervaults.bukkit.vault;

import com.github.dig.endervaults.api.EnderVaultsPlugin;
import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.vault.Vault;
import java.util.Map;
import java.util.UUID;

public class BukkitVaultFactory {

    private static final EnderVaultsPlugin plugin = PluginProvider.getPlugin();

    public static Vault create(UUID ownerUUID, Map<String, Object> metadata) {
        String title = plugin.getLanguage().get(Lang.VAULT_TITLE, metadata);
        return null;
    }
}
