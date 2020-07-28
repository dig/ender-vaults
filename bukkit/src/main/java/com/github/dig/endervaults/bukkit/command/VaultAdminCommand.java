package com.github.dig.endervaults.bukkit.command;

import com.github.dig.endervaults.api.EnderVaultsPlugin;
import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.lang.Language;
import com.github.dig.endervaults.api.permission.UserPermission;
import com.github.dig.endervaults.api.vault.metadata.VaultDefaultMetadata;
import com.github.dig.endervaults.bukkit.ui.selector.SelectorInventory;
import com.github.dig.endervaults.bukkit.vault.BukkitVault;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class VaultAdminCommand implements CommandExecutor {

    private final EnderVaultsPlugin plugin = PluginProvider.getPlugin();
    private final Language language = plugin.getLanguage();
    private final UserPermission<Player> permission = plugin.getPermission();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (permission.isVaultAdmin(player)) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.RED + "Usage: /pvadmin <name> [vault]");
                } else if (args.length == 1) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        sender.sendMessage(language.get(Lang.PLAYER_NOT_FOUND));
                        return true;
                    }

                    String title = language.get(Lang.ADMIN_VAULT_SELECTOR_TITLE, new HashMap<String, Object>(){{
                        put("player", target.getName());
                    }});
                    new SelectorInventory(target.getUniqueId(), 1, title).launchFor(player);
                } else if (args.length == 2) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        sender.sendMessage(language.get(Lang.PLAYER_NOT_FOUND));
                        return true;
                    }

                    int vaultOrder;
                    try {
                        vaultOrder = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(language.get(Lang.INVALID_VAULT_ORDER));
                        return true;
                    }

                    plugin.getRegistry().getByMetadata(target.getUniqueId(), VaultDefaultMetadata.ORDER.getKey(), vaultOrder)
                            .ifPresent(vault -> ((BukkitVault) vault).launchFor(player));
                }
            } else {
                sender.sendMessage(language.get(Lang.NO_PERMISSION));
            }
        }
        return true;
    }
}
