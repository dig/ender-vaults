package com.github.dig.endervaults.bukkit;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.vault.VaultPersister;
import com.github.dig.endervaults.bukkit.vault.BukkitVaultRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.stream.Collectors;

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        BukkitVaultRegistry registry = (BukkitVaultRegistry) plugin.getRegistry();
        ItemStack item = event.getCurrentItem();
        Inventory inventory = event.getInventory();

        if (inventory != null && item != null) {
            if (registry.isVault(inventory) && getBlacklisted().contains(item.getType())) {
                player.sendMessage(plugin.getLanguage().get(Lang.BLACKLISTED_ITEM));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(InventoryMoveItemEvent event) {
        BukkitVaultRegistry registry = (BukkitVaultRegistry) plugin.getRegistry();
        ItemStack item = event.getItem();
        Inventory inventory = event.getDestination();

        if (inventory != null && item != null) {
            if (registry.isVault(inventory) && getBlacklisted().contains(item.getType())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();

        BukkitVaultRegistry registry = (BukkitVaultRegistry) plugin.getRegistry();
        ItemStack item = event.getCursor();
        Inventory inventory = event.getInventory();

        if (inventory != null && item != null) {
            if (registry.isVault(inventory) && getBlacklisted().contains(item.getType())) {
                player.sendMessage(plugin.getLanguage().get(Lang.BLACKLISTED_ITEM));
                event.setCancelled(true);
            }
        }
    }

    private Set<Material> getBlacklisted() {
        FileConfiguration configuration = (FileConfiguration) plugin.getConfigFile().getConfiguration();
        return configuration.getStringList("vault.blacklist")
                .stream()
                .map(Material::valueOf)
                .collect(Collectors.toSet());
    }
}
