package com.github.dig.endervaults.bukkit.command;

import com.github.dig.endervaults.api.VaultPluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.lang.Language;
import com.github.dig.endervaults.api.permission.UserPermission;
import com.github.dig.endervaults.bukkit.EVBukkitPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class VaultReloadCommand implements CommandExecutor {

    private final EVBukkitPlugin plugin = (EVBukkitPlugin) VaultPluginProvider.getPlugin();
    private final Language language = plugin.getLanguage();
    private final UserPermission<Player> permission = plugin.getPermission();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender || permission.canReload((Player) sender)) {
            plugin.getConfigFile().load();
            plugin.getLangFile().load();
            sender.sendMessage(language.get(Lang.CONFIG_RELOAD));
        } else {
            sender.sendMessage(language.get(Lang.NO_PERMISSION));
        }

        return true;
    }
}
