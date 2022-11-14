package com.github.dig.endervaults.bukkit.ui.icon;

import com.github.dig.endervaults.api.selector.SelectorMode;
import com.github.dig.endervaults.api.vault.Vault;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SelectIconBuilder {


    public static NBTItem fromMaterial(Vault vault, Material material) {
        return fromMaterial(vault, material, SelectorMode.STATIC);
    }

    public static NBTItem fromMaterial(Vault vault, Material material, SelectorMode selectorMode) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_DESTROYS,
                ItemFlag.HIDE_POTION_EFFECTS,
                ItemFlag.HIDE_PLACED_ON);
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setBoolean(SelectIconConstants.NBT_ICON_ITEM, true);
        nbtItem.setString(SelectIconConstants.NBT_ICON_ID, vault.getId().toString());
        nbtItem.setString(SelectIconConstants.NBT_ICON_OWNER_UUID, vault.getOwner().toString());
        nbtItem.setString(SelectIconConstants.NBT_ICON_SELECTOR_MODE, selectorMode.name());
        return nbtItem;
    }
}
