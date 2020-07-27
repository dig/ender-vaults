package com.github.dig.endervaults.bukkit.command;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.lang.Language;
import com.github.dig.endervaults.bukkit.EVBukkitPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VaultReloadCommand implements CommandExecutor {

    private final EVBukkitPlugin plugin = (EVBukkitPlugin) PluginProvider.getPlugin();
    private final Language language = plugin.getLanguage();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("endervaults.admin.reload")) {
            plugin.getConfigFile().load();
            plugin.getLangFile().load();
            sender.sendMessage(language.get(Lang.CONFIG_RELOAD));
        } else {
            sender.sendMessage(language.get(Lang.NO_PERMISSION));
        }

        return true;
    }
}
