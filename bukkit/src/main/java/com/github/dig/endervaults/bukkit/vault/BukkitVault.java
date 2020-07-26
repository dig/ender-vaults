package com.github.dig.endervaults.bukkit.vault;

import com.github.dig.endervaults.api.EnderVaultsPlugin;
import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.exception.InvalidMinecraftVersionException;
import com.github.dig.endervaults.api.nms.VaultNMS;
import com.github.dig.endervaults.api.util.VaultSerializable;
import com.github.dig.endervaults.api.vault.Vault;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

@Log
public class BukkitVault implements Vault, VaultSerializable {

    private UUID id;
    private UUID ownerUUID;
    private Inventory inventory;

    private VaultNMS nmsBridge = null;

    public BukkitVault(UUID id, String title, int size, UUID ownerUUID) {
        this.id = id;
        this.ownerUUID = ownerUUID;
        this.inventory = Bukkit.createInventory(null, size, title);
        try {
            this.nmsBridge = PluginProvider.getPlugin().getNMSBridge();
        } catch (InvalidMinecraftVersionException e) {
            log.log(Level.SEVERE, "Unable to get NMS Bridge for your Minecraft version.");
        }
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
    public String encode() {
        ItemStack[] inventoryContents = inventory.getContents();

        Class<?> nmsItemStackClazz;
        try {
            nmsItemStackClazz = nmsBridge.getItemStackClass();
        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "Unable to find craft item stack class.", e);
            return null;
        }

        Object nmsItemArray = Array.newInstance(nmsItemStackClazz, inventoryContents.length);
        for (int i = 0; i < inventoryContents.length; i++) {
            try {
                Array.set(nmsItemArray, i, toNMSItem(inventoryContents[i]));
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                log.log(Level.SEVERE, "Unable to encode item.", e);
            }
        }

        return nmsBridge.encode((Object[]) nmsItemArray);
    }

    @Override
    public void decode(String encoded) {
        Object[] nmsItemStacks = nmsBridge.decode(encoded);
        ItemStack[] inventoryContents = new ItemStack[nmsItemStacks.length];
        for (int i = 0; i < nmsItemStacks.length; i++) {
            try {
                inventoryContents[i] = toBukkitItem(nmsItemStacks[i]);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                log.log(Level.SEVERE, "Unable to decode item.", e);
            }
        }

        inventory.setContents(inventoryContents);
    }

    private ItemStack toBukkitItem(Object itemStack) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> nmsItemStackClazz;
        try {
            nmsItemStackClazz = nmsBridge.getItemStackClass();
        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "Unable to find craft item stack class.", e);
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
        return Class.forName("org.bukkit.craftbukkit." + plugin.getServerVersion().toString() + ".inventory.CraftItemStack");
    }
}
