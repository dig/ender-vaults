package com.github.dig.endervaults.bukkit.selector;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.permission.UserPermission;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.api.vault.VaultRegistry;
import com.github.dig.endervaults.api.vault.metadata.VaultDefaultMetadata;
import com.github.dig.endervaults.bukkit.vault.BukkitVault;
import com.github.dig.endervaults.bukkit.vault.BukkitVaultFactory;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
    private final UserPermission permission = PluginProvider.getPlugin().getPermission();

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        ItemStack item = event.getCurrentItem();

        if (inventory != null && item != null && item.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasKey(SelectorConstants.NBT_VAULT_ITEM)) {
                event.setCancelled(true);
                if (nbtItem.hasKey(SelectorConstants.NBT_VAULT_ID) && nbtItem.hasKey(SelectorConstants.NBT_VAULT_OWNER_UUID)) {
                    UUID vaultID = UUID.fromString(nbtItem.getString(SelectorConstants.NBT_VAULT_ID));
                    UUID vaultOwnerUUID = UUID.fromString(nbtItem.getString(SelectorConstants.NBT_VAULT_OWNER_UUID));

                    registry.get(vaultOwnerUUID, vaultID).ifPresent(vault -> {
                        BukkitVault bukkitVault = (BukkitVault) vault;
                        if (permission.canUseVault(player, (int) bukkitVault.getMetadata().get(VaultDefaultMetadata.ORDER.getKey()))) {
                            bukkitVault.launchFor(player);
                        }
                    });
                } else if (nbtItem.hasKey(SelectorConstants.NBT_VAULT_ORDER) && nbtItem.hasKey(SelectorConstants.NBT_VAULT_OWNER_UUID)) {
                    int orderValue = nbtItem.getInteger(SelectorConstants.NBT_VAULT_ORDER);
                    UUID vaultOwnerUUID = UUID.fromString(nbtItem.getString(SelectorConstants.NBT_VAULT_OWNER_UUID));

                    if (!permission.canUseVault(player, orderValue) && !permission.isVaultAdmin(player)) {
                        return;
                    }

                    Optional<Vault> vaultOptional = registry
                            .getByMetadata(vaultOwnerUUID, VaultDefaultMetadata.ORDER.getKey(), orderValue);

                    BukkitVault vault;
                    if (vaultOptional.isPresent()) {
                        vault = (BukkitVault) vaultOptional.get();
                    } else {
                        vault = (BukkitVault) BukkitVaultFactory.create(vaultOwnerUUID, new HashMap<String, Object>(){{
                            put(VaultDefaultMetadata.ORDER.getKey(), orderValue);
                        }});
                        registry.register(vaultOwnerUUID, vault);
                    }

                    vault.launchFor(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
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
