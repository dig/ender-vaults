package com.github.dig.endervaults.bukkit.selector;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.selector.SelectorMode;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.api.vault.VaultRegistry;
import com.github.dig.endervaults.api.vault.metadata.VaultDefaultMetadata;
import com.github.dig.endervaults.bukkit.EVBukkitPlugin;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class SelectorInventory {

    private final EVBukkitPlugin plugin = (EVBukkitPlugin) PluginProvider.getPlugin();
    private final VaultRegistry registry = plugin.getRegistry();

    private final UUID ownerUUID;
    private final int page;
    private final SelectorMode mode;
    private final Inventory inventory;

    public SelectorInventory(UUID ownerUUID, int page) {
        FileConfiguration configuration = (FileConfiguration) plugin.getConfigFile().getConfiguration();
        int size = configuration.getInt("selector.rows", 6) * 9;
        String title = plugin.getLanguage().get(Lang.VAULT_SELECTOR_TITLE);

        this.ownerUUID = ownerUUID;
        this.page = page;
        this.mode = SelectorMode.valueOf(configuration.getString("selector.design-mode", SelectorMode.PANE_BY_FILL.toString()));
        this.inventory = Bukkit.createInventory(null, size, title);

        init();
    }

    public SelectorInventory(UUID ownerUUID, int page, String title) {
        FileConfiguration configuration = (FileConfiguration) plugin.getConfigFile().getConfiguration();
        int size = configuration.getInt("selector.rows", 6) * 9;

        this.ownerUUID = ownerUUID;
        this.page = page;
        this.mode = SelectorMode.valueOf(configuration.getString("selector.design-mode", SelectorMode.PANE_BY_FILL.toString()));
        this.inventory = Bukkit.createInventory(null, size, title);

        init();
    }

    private void init() {
        FileConfiguration configuration = (FileConfiguration) plugin.getConfigFile().getConfiguration();
        // ItemStack locked = createLockedItem();
        for (int i = 0; i < inventory.getSize(); i++) {
            int order = page > 1 ? ((page - 1) * inventory.getSize()) + i : ((page - 1) * inventory.getSize()) + (i + 1);

            UUID id = null;
            int size = configuration.getInt("vault.default-rows", 3) * 9;
            int free = 0;
            int filled = 0;

            Optional<Vault> vaultOptional = registry.getByMetadata(ownerUUID, VaultDefaultMetadata.ORDER.getKey(), order);
            if (vaultOptional.isPresent()) {
                Vault vault = vaultOptional.get();

                id = vault.getId();
                size = vault.getSize();
                free = vault.getFreeSize();
                filled = size - free;
            }

            inventory.setItem(i, createUnlockedItem(id, order, size, free, filled));
        }
    }

    private ItemStack createUnlockedItem(@Nullable UUID id, int order, int size, int free, int filled) {
        FileConfiguration configuration = (FileConfiguration) plugin.getConfigFile().getConfiguration();

        Material material;
        switch (mode) {
            case STATIC:
                material = Material.valueOf(configuration.getString("selector.static-item.unlocked", Material.CHEST.toString()));
                break;
            case PANE_BY_FILL:
            default:
                material = getGlass(filled, size);
                break;
        }

        ItemStack item = new ItemStack(material, 1);

        ItemMeta meta = item.getItemMeta();
        String title = configuration.getString("selector.template.unlocked.title")
                .replaceAll("%order", String.valueOf(order));
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
        meta.setLore(configuration.getStringList("selector.template.unlocked.lore")
                .stream()
                .map(s -> s.replaceAll("%filled_slots", String.valueOf(filled)))
                .map(s -> s.replaceAll("%total_slots", String.valueOf(size)))
                .map(s -> s.replaceAll("%free_slots", String.valueOf(free)))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList()));
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setBoolean(SelectorConstants.NBT_VAULT_ITEM, true);
        if (id != null) {
            nbtItem.setString(SelectorConstants.NBT_VAULT_ID, id.toString());
        }
        nbtItem.setString(SelectorConstants.NBT_VAULT_OWNER_UUID, ownerUUID.toString());
        nbtItem.setInteger(SelectorConstants.NBT_VAULT_ORDER, order);
        return nbtItem.getItem();
    }

    private Material getGlass(int filled, int total) {
        double percent = ((double) filled / (double) total) * 100;
        if (percent >= 100) {
            return Material.RED_STAINED_GLASS_PANE;
        } else if (percent > 60) {
            return Material.ORANGE_STAINED_GLASS_PANE;
        } else if (percent > 30) {
            return Material.YELLOW_STAINED_GLASS_PANE;
        } else if (percent > 0) {
            return Material.LIME_STAINED_GLASS_PANE;
        }
        return Material.WHITE_STAINED_GLASS_PANE;
    }

    private ItemStack createLockedItem() {
        FileConfiguration configuration = (FileConfiguration) plugin.getConfigFile().getConfiguration();

        Material material;
        switch (mode) {
            case STATIC:
                material = Material.valueOf(configuration.getString("selector.static-item.locked", Material.REDSTONE_BLOCK.toString()));
                break;
            case PANE_BY_FILL:
            default:
                material = Material.GRAY_STAINED_GLASS_PANE;
                break;
        }

        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        String title = configuration.getString("selector.template.locked.title");
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
        meta.setLore(configuration.getStringList("selector.template.locked.lore")
                        .stream()
                        .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                        .collect(Collectors.toList()));
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setBoolean(SelectorConstants.NBT_VAULT_ITEM, true);
        return nbtItem.getItem();
    }

    public void launchFor(Player player) {
        player.openInventory(inventory);
    }
}
