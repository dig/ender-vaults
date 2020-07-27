package com.github.dig.endervaults.bukkit;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.vault.VaultPersister;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BukkitListener implements Listener {

    private final EVBukkitPlugin plugin = (EVBukkitPlugin) PluginProvider.getPlugin();
    private final VaultPersister persister = plugin.getPersister();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> persister.load(event.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> persister.save(event.getPlayer().getUniqueId()));
    }
}
