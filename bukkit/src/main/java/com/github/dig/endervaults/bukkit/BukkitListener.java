package com.github.dig.endervaults.bukkit;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.vault.VaultPersister;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BukkitListener implements Listener {

    private final VaultPersister persister = PluginProvider.getPlugin().getPersister();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        persister.load(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        persister.save(event.getPlayer().getUniqueId());
    }
}
