package com.github.dig.endervaults.bukkit.selector;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.api.vault.VaultRegistry;
import com.github.dig.endervaults.api.vault.metadata.VaultDefaultMetadata;
import com.github.dig.endervaults.bukkit.vault.BukkitVault;
import com.github.dig.endervaults.bukkit.vault.BukkitVaultFactory;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class SelectorListener implements Listener {

    private final VaultRegistry registry = PluginProvider.getPlugin().getRegistry();

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        ItemStack item = event.getCurrentItem();

        if (inventory != null && item != null && item.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasKey(SelectorConstants.NBT_VAULT_ITEM)) {
                if (nbtItem.hasKey(SelectorConstants.NBT_VAULT_ID) && nbtItem.hasKey(SelectorConstants.NBT_VAULT_OWNER_UUID)) {
                    UUID vaultID = UUID.fromString(nbtItem.getString(SelectorConstants.NBT_VAULT_ID));
                    UUID vaultOwnerUUID = UUID.fromString(nbtItem.getString(SelectorConstants.NBT_VAULT_OWNER_UUID));

                    registry.get(vaultOwnerUUID, vaultID).ifPresent(vault -> ((BukkitVault) vault).launchFor(player));
                    event.setCancelled(true);
                } else if (nbtItem.hasKey(SelectorConstants.NBT_VAULT_ORDER)) {
                    int orderValue = nbtItem.getInteger(SelectorConstants.NBT_VAULT_ORDER);
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
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onMove(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasKey(SelectorConstants.NBT_VAULT_ITEM)) {
                event.setCancelled(true);
            }
        }
    }
}
