package com.github.dig.endervaults.bukkit.ui.icon;

import com.github.dig.endervaults.api.VaultPluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.bukkit.EVBukkitPlugin;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
        for (String matName : configuration.getStringList("selector.select-icon.items")) {
            Material material;
            try {
                material = Material.valueOf(matName);
            } catch (IllegalArgumentException e) {
                log.log(Level.SEVERE, "[EnderVaults] Unable to find material " + matName + ", skipping...", e);
                continue;
            }

            ItemStack item = new ItemStack(material, 1);

            NBTItem nbtItem = new NBTItem(item);
            nbtItem.setBoolean(SelectIconConstants.NBT_ICON_ITEM, true);
            nbtItem.setString(SelectIconConstants.NBT_ICON_ID, vault.getId().toString());
            nbtItem.setString(SelectIconConstants.NBT_ICON_OWNER_UUID, vault.getOwner().toString());

            int slot = inventory.firstEmpty();
            if (slot > -1) {
                inventory.setItem(inventory.firstEmpty(), nbtItem.getItem());
            } else {
                log.log(Level.INFO, "[EnderVaults] Unable to find available spot to put item in, please reconfigure select icon settings.");
            }
        }
    }

    public void launchFor(Player player) {
        player.openInventory(inventory);
    }
}
