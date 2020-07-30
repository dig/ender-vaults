package com.github.dig.endervaults.bukkit.vault;

import com.github.dig.endervaults.api.EnderVaultsPlugin;
import com.github.dig.endervaults.api.VaultPluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.vault.Vault;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;
import java.util.UUID;

public class BukkitVaultFactory {

    private static final EnderVaultsPlugin plugin = VaultPluginProvider.getPlugin();

    public static Vault create(UUID ownerUUID, Map<String, Object> metadata) {
        FileConfiguration configuration = (FileConfiguration) plugin.getConfigFile().getConfiguration();

        String title = plugin.getLanguage().get(Lang.VAULT_TITLE, metadata);
        int size = configuration.getInt("vault.default-rows", 3) * 9;

        return new BukkitVault(UUID.randomUUID(), title, size, ownerUUID, metadata);
    }
}
