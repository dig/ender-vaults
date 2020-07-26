package com.github.dig.endervaults.nms.v1_16_R1;

import com.github.dig.endervaults.api.nms.VaultNMS;
import lombok.extern.java.Log;
import net.minecraft.server.v1_16_R1.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.logging.Level;

@Log
public class v1_16_R1NMS implements VaultNMS {

    private static Method writeNbt;
    private static Method readNbt;

    static {
        try {
            writeNbt = NBTCompressedStreamTools.class.getDeclaredMethod("a", NBTBase.class, DataOutput.class);
            writeNbt.setAccessible(true);

            readNbt = NBTCompressedStreamTools.class.getDeclaredMethod("a", DataInput.class, Integer.TYPE, NBTReadLimiter.class);
            readNbt.setAccessible(true);
        } catch (NoSuchMethodException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to find writeNbt or readNbt method. Are you sure we support this minecraft version?", e);
        }
    }

    @Override
    public String encode(Object[] craftItemStacks) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        NBTTagList nbtTagList = new NBTTagList();

        for (int i = 0; i < craftItemStacks.length; ++i) {
            ItemStack itemStack = (ItemStack) craftItemStacks[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (itemStack != null) {
                itemStack.save(nbtTagCompound);
            }
            nbtTagList.add(nbtTagCompound);
        }

        try {
            writeNbt.invoke(null, nbtTagList, dataOutputStream);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to invoke writeNbt.", e);
            return null;
        }

        return new String(Base64.getEncoder().encode(byteArrayOutputStream.toByteArray()));
    }

    @Override
    public Object[] decode(String encoded) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(encoded));
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        NBTTagList nbtTagList;
        try {
            nbtTagList = (NBTTagList) readNbt.invoke(null, dataInputStream, 0, new NBTReadLimiter(Long.MAX_VALUE));
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to invoke readNbt.", e);
            return null;
        }

        ItemStack[] items = new ItemStack[nbtTagList.size()];
        for (int i = 0; i < nbtTagList.size(); ++i) {
            NBTTagCompound nbtTagCompound = (NBTTagCompound) nbtTagList.get(i);
            if (!nbtTagCompound.isEmpty()) {
                items[i] = ItemStack.a(nbtTagCompound);
            }
        }

        return items;
    }
}
