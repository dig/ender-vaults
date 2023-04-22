package com.github.dig.endervaults.bukkit.ui.icon;

import com.github.dig.endervaults.api.VaultPluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.selector.SelectorMode;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.bukkit.EVBukkitPlugin;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.logging.Level;

@Log
public class SelectIconInventory {

    private final EVBukkitPlugin plugin = (EVBukkitPlugin) VaultPluginProvider.getPlugin();
    private final FileConfiguration configuration = (FileConfiguration) plugin.getConfigFile().getConfiguration();

    private final Vault vault;
    private final Inventory inventory;

    public SelectIconInventory(Vault vault) {
        int size = configuration.getInt("selector.select-icon.rows", 3) * 9;
        this.vault = vault;
        this.inventory = Bukkit.createInventory(null, size,
                plugin.getLanguage().get(Lang.VAULT_SELECT_ICON_TITLE));
        init();
    }

    private void init() {
        int slot;
        Material material;
        for (String matName : configuration.getStringList("selector.select-icon.items")) {
            try {
                material = Material.valueOf(matName);
            } catch (IllegalArgumentException e) {
                log.log(Level.SEVERE, "[EnderVaults] Unable to find material " + matName + ", skipping...", e);
                continue;
            }
            slot = inventory.firstEmpty();
            addItemAt(SelectIconBuilder.fromMaterial(vault, material, SelectorMode.STATIC), slot);
        }
        for (String matName : configuration.getStringList("selector.select-icon.pane_fill_item")) {
            try {
                material = Material.valueOf(matName);
            } catch (IllegalArgumentException e) {
                log.log(Level.SEVERE, "[EnderVaults] Unable to find material " + matName + ", skipping...", e);
                continue;
            }
            slot = inventory.firstEmpty();
            addItemAt(SelectIconBuilder.fromMaterial(vault, material, SelectorMode.PANE_BY_FILL), slot);
        }
    }

    private void addItemAt(NBTItem nbtItem, int slot) {
        if (slot > -1) {
            inventory.setItem(inventory.firstEmpty(), nbtItem.getItem());
        } else {
            log.log(Level.INFO, "[EnderVaults] Unable to find available spot to put item in, please reconfigure select icon settings.");
        }
    }

    public void launchFor(Player player) {
        player.openInventory(inventory);
    }
}
