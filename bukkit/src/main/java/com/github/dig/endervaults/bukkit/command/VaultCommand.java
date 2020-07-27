package com.github.dig.endervaults.bukkit.command;

import com.github.dig.endervaults.api.EnderVaultsPlugin;
import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.api.vault.VaultRegistry;
import com.github.dig.endervaults.api.vault.metadata.VaultDefaultMetadata;
import com.github.dig.endervaults.bukkit.vault.BukkitVault;
import com.github.dig.endervaults.bukkit.vault.BukkitVaultFactory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Optional;

public class VaultCommand implements CommandExecutor {

    private final EnderVaultsPlugin plugin = PluginProvider.getPlugin();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!plugin.getPersister().isLoaded(player.getUniqueId())) {
                sender.sendMessage(plugin.getLanguage().get(Lang.PLAYER_NOT_LOADED));
                return true;
            }

            if (args.length == 1) {
                VaultRegistry registry = plugin.getRegistry();

                int orderValue;
                try {
                    orderValue = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getLanguage().get(Lang.INVALID_VAULT_ORDER));
                    return true;
                }

                if (orderValue <= 0) {
                    sender.sendMessage(plugin.getLanguage().get(Lang.INVALID_VAULT_ORDER));
                    return true;
                }

                Optional<Vault> vaultOptional = registry
                        .getByMetadata(player.getUniqueId(), VaultDefaultMetadata.ORDER.getKey(), orderValue);

                BukkitVault vault;
                if (vaultOptional.isPresent()) {
                    vault = (BukkitVault) vaultOptional.get();
                } else {
                    vault = (BukkitVault) BukkitVaultFactory.create(player.getUniqueId(), new HashMap<String, Object>(){{
                        put(VaultDefaultMetadata.ORDER.getKey(), orderValue);
                    }});
                    registry.register(player.getUniqueId(), vault);
                }

                vault.launchFor(player);
            } else {
                // TODO: open selector
            }
        }
        return true;
    }
}
