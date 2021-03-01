package com.github.dig.endervaults.bukkit.vault;

import lombok.NonNull;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BukkitInventoryHolder implements InventoryHolder {

    private final BukkitVault vault;
    public BukkitInventoryHolder(@NonNull BukkitVault vault) {
        this.vault = vault;
    }

    @Override
    public Inventory getInventory() {
        return vault.getInventory();
    }
}
