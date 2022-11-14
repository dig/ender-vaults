package com.github.dig.endervaults.bukkit;

import com.github.dig.endervaults.api.VaultPluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.permission.UserPermission;
import com.github.dig.endervaults.api.vault.VaultPersister;
import com.github.dig.endervaults.bukkit.ui.selector.SelectorInventory;
import com.github.dig.endervaults.bukkit.vault.BukkitVaultRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class BukkitListener implements Listener {

    private final EVBukkitPlugin plugin = (EVBukkitPlugin) VaultPluginProvider.getPlugin();
    private final VaultPersister persister = plugin.getPersister();
    private final UserPermission<Player> permission = plugin.getPermission();

    private final Map<UUID, BukkitTask> pendingLoadMap = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfigFile().getConfiguration();
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
                () -> persister.load(player.getUniqueId()),
                config.getLong("storage.settings.load-delay", 5 * 20));
        pendingLoadMap.put(player.getUniqueId(), bukkitTask);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (pendingLoadMap.containsKey(player.getUniqueId())) {
            pendingLoadMap.remove(player.getUniqueId()).cancel();
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin,
                () -> persister.save(player.getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        BukkitVaultRegistry registry = (BukkitVaultRegistry) plugin.getRegistry();
        ArrayList<ItemStack> items = new ArrayList<>();
        items.add(event.getCurrentItem());
        Inventory inventory = event.getInventory();

        if(event.getClick() == ClickType.NUMBER_KEY) {
            items.add(player.getInventory().getItem(event.getHotbarButton()));
        }

        if (inventory != null && isBlacklistEnabled()) {
            if(permission.canBypassBlacklist(player)) return;
            if(!registry.isVault(inventory)) return;

            for(ItemStack item : items) {
                if (item != null && getBlacklisted().contains(item.getType())) {
                    player.sendMessage(plugin.getLanguage().get(Lang.BLACKLISTED_ITEM));
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(InventoryMoveItemEvent event) {
        BukkitVaultRegistry registry = (BukkitVaultRegistry) plugin.getRegistry();
        ItemStack item = event.getItem();
        Inventory inventory = event.getDestination();

        if (inventory != null && item != null && isBlacklistEnabled()) {
            if (getBlacklisted().contains(item.getType()) && registry.isVault(inventory)) {
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

        if (inventory != null && item != null && isBlacklistEnabled()) {
            if (!permission.canBypassBlacklist(player) && getBlacklisted().contains(item.getType()) && registry.isVault(inventory)) {
                player.sendMessage(plugin.getLanguage().get(Lang.BLACKLISTED_ITEM));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.ENDER_CHEST && isEnderchestReplaced()) {
            event.setCancelled(true);
            if (!persister.isLoaded(player.getUniqueId())) {
                player.sendMessage(plugin.getLanguage().get(Lang.PLAYER_NOT_LOADED));
                return;
            }
            new SelectorInventory(player.getUniqueId(), 1).launchFor(player);
        }
    }

    private boolean isEnderchestReplaced() {
        FileConfiguration configuration = (FileConfiguration) plugin.getConfigFile().getConfiguration();
        return configuration.getBoolean("enderchest.replace-with-selector", false);
    }

    private boolean isBlacklistEnabled() {
        FileConfiguration configuration = (FileConfiguration) plugin.getConfigFile().getConfiguration();
        return configuration.getBoolean("vault.blacklist.enabled", false);
    }

    private Set<Material> getBlacklisted() {
        FileConfiguration configuration = (FileConfiguration) plugin.getConfigFile().getConfiguration();
        return configuration.getStringList("vault.blacklist.items")
                .stream()
                .map(Material::valueOf)
                .collect(Collectors.toSet());
    }
}
