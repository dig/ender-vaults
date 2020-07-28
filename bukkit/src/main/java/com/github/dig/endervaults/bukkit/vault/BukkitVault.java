package com.github.dig.endervaults.bukkit.vault;

import com.github.dig.endervaults.api.EnderVaultsPlugin;
import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.nms.NMSProvider;
import com.github.dig.endervaults.nms.VaultNMS;
import com.github.dig.endervaults.api.util.VaultSerializable;
import com.github.dig.endervaults.api.vault.Vault;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

@Log
public class BukkitVault implements Vault, VaultSerializable {

    private UUID id;
    private UUID ownerUUID;
    private Inventory inventory;
    private Map<String, Object> metadata;

    private final VaultNMS nmsBridge = NMSProvider.getBridge();

    public BukkitVault(UUID id, String title, int size, UUID ownerUUID) {
        this.id = id;
        this.ownerUUID = ownerUUID;
        this.inventory = Bukkit.createInventory(null, size, title);
        this.metadata = new HashMap<>();
    }

    public BukkitVault(UUID id, String title, int size, UUID ownerUUID, Map<String, Object> metadata) {
        this(id, title, size, ownerUUID);
        this.metadata = metadata;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public UUID getOwner() {
        return ownerUUID;
    }

    @Override
    public int getSize() {
        return inventory.getSize();
    }

    @Override
    public int getFreeSize() {
        int free = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                free++;
            }
        }
        return free;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    @Nullable
    public String encode() {
        ItemStack[] inventoryContents = inventory.getContents();

        Class<?> nmsItemStackClazz;
        try {
            nmsItemStackClazz = nmsBridge.getItemStackClass();
        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to find craft item stack class.", e);
            return null;
        }

        Object nmsItemArray = Array.newInstance(nmsItemStackClazz, inventoryContents.length);
        for (int i = 0; i < inventoryContents.length; i++) {
            try {
                Array.set(nmsItemArray, i, toNMSItem(inventoryContents[i]));
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                log.log(Level.SEVERE, "[EnderVaults] Unable to encode item.", e);
            }
        }

        try {
            return nmsBridge.encode((Object[]) nmsItemArray);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to encode bukkit vault.", e);
            return null;
        }
    }

    @Override
    public void decode(String encoded) {
        Object[] nmsItemStacks;
        try {
            nmsItemStacks = nmsBridge.decode(encoded);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to decode bukkit vault.", e);
            return;
        }

        ItemStack[] inventoryContents = new ItemStack[nmsItemStacks.length];
        for (int i = 0; i < nmsItemStacks.length; i++) {
            try {
                inventoryContents[i] = toBukkitItem(nmsItemStacks[i]);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                log.log(Level.SEVERE, "[EnderVaults] Unable to decode item.", e);
            }
        }

        inventory.setContents(inventoryContents);
    }

    public void launchFor(Player player) {
        player.openInventory(inventory);
    }

    public boolean compare(Inventory inventory) {
        return this.inventory == inventory;
    }

    private ItemStack toBukkitItem(Object itemStack) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> nmsItemStackClazz;
        try {
            nmsItemStackClazz = nmsBridge.getItemStackClass();
        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to find craft item stack class.", e);
            return null;
        }

        Method asBukkitCopy = getCraftItemStackClass().getMethod("asBukkitCopy", nmsItemStackClazz);
        return (ItemStack) asBukkitCopy.invoke(null, itemStack);
    }

    private Object toNMSItem(ItemStack itemStack) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method asNMSCopy = getCraftItemStackClass().getMethod("asNMSCopy", ItemStack.class);
        return asNMSCopy.invoke(null, itemStack);
    }

    private Class<?> getCraftItemStackClass() throws ClassNotFoundException {
        EnderVaultsPlugin plugin = PluginProvider.getPlugin();
        return Class.forName("org.bukkit.craftbukkit." + plugin.getVersion().toString() + ".inventory.CraftItemStack");
    }
}
