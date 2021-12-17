package com.github.dig.endervaults.bukkit.command;

import com.github.dig.endervaults.api.EnderVaultsPlugin;
import com.github.dig.endervaults.api.VaultPluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.migration.Migrator;
import com.github.dig.endervaults.bukkit.migration.MigrationPlugins;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Optional;

public class VaultMigrateCommand implements CommandExecutor {

    private final EnderVaultsPlugin plugin = VaultPluginProvider.getPlugin();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            if (args.length == 1) {
                MigrationPlugins type;

                try {
                    type = MigrationPlugins.valueOf(args[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "No migration for that plugin, contact us on discord for migration from other plugins.");
                    return true;
                }

                if (Bukkit.getOnlinePlayers().size() > 0) {
                    sender.sendMessage(ChatColor.RED + "Cannot migrate whilst players are on the server.");
                    return true;
                }

                Migrator migrator = type.get();

                if (migrator.can()) {
                    migrator.migrate();
                } else {
                    sender.sendMessage(ChatColor.RED + migrator.response());
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /pvmigrate <plugin>");
            }
        } else {
            sender.sendMessage(plugin.getLanguage().get(Lang.ONLY_FROM_CONSOLE));
        }

        return true;
    }
}
