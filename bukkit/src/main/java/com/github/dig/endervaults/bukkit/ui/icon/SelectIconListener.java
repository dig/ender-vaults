package com.github.dig.endervaults.bukkit.ui.icon;

import com.github.dig.endervaults.api.VaultPluginProvider;
import com.github.dig.endervaults.api.selector.SelectorMode;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.api.vault.VaultRegistry;
import com.github.dig.endervaults.api.vault.metadata.VaultDefaultMetadata;
import com.github.dig.endervaults.bukkit.ui.selector.SelectorInventory;
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

import java.util.UUID;

public class SelectIconListener implements Listener {

    private final VaultRegistry registry = VaultPluginProvider.getPlugin().getRegistry();

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        ItemStack item = event.getCurrentItem();

        if (inventory != null && item != null && item.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasKey(SelectIconConstants.NBT_ICON_ITEM) && nbtItem.hasKey(SelectIconConstants.NBT_ICON_ID)) {
                event.setCancelled(true);
                UUID vaultID = UUID.fromString(nbtItem.getString(SelectIconConstants.NBT_ICON_ID));
                UUID vaultOwnerUUID = UUID.fromString(nbtItem.getString(SelectIconConstants.NBT_ICON_OWNER_UUID));
                SelectorMode selectorMode = SelectorMode.valueOf(nbtItem.getString(SelectIconConstants.NBT_ICON_SELECTOR_MODE));

                switch(selectorMode) {
                    case STATIC: {
                        registry.get(vaultOwnerUUID, vaultID).ifPresent(vault -> vault.getMetadata().put(VaultDefaultMetadata.ICON.getKey(), item.getType().toString()));
                    }
                    case PANE_BY_FILL: {
                        registry.get(vaultOwnerUUID, vaultID).ifPresent(vault -> vault.getMetadata().remove(VaultDefaultMetadata.ICON.getKey()));
                    }
                }
                new SelectorInventory(vaultOwnerUUID, 1).launchFor(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasKey(SelectIconConstants.NBT_ICON_ITEM)) {
                event.setCancelled(true);
            }
        }
    }
}
