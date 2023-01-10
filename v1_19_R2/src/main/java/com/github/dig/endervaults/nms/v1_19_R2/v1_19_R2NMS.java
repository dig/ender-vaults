package com.github.dig.endervaults.nms.v1_19_R2;

import com.github.dig.endervaults.nms.MinecraftVersion;
import com.github.dig.endervaults.nms.VaultNMS;
import lombok.extern.java.Log;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.logging.Level;

@Log
public class v1_19_R2NMS implements VaultNMS {

    private Class<?> nbtTagListClass;
    private Class<?> nbtItemStackClass;
    private Class<?> nbtBaseClass;
    private Class<?> nbtTagCompoundClass;
    private Class<?> nbtReadLimiterClass;

    private Method writeNbt;
    private Method readNbt;

    @Override
    public boolean init(MinecraftVersion version) {
        Class<?> nbtToolsClass;
        try {
            nbtToolsClass = Class.forName("net.minecraft.nbt.NBTCompressedStreamTools");

            nbtReadLimiterClass = Class.forName("net.minecraft.nbt.NBTReadLimiter");
            nbtTagListClass = Class.forName("net.minecraft.nbt.NBTTagList");
            nbtItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
            nbtBaseClass = Class.forName("net.minecraft.nbt.NBTBase");
            nbtTagCompoundClass = Class.forName("net.minecraft.nbt.NBTTagCompound");
        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to find classes needed for NBT. Are you sure we support this Minecraft version?", e);
            return false;
        }

        try {
            writeNbt = nbtToolsClass.getDeclaredMethod("a", nbtBaseClass, DataOutput.class);
            writeNbt.setAccessible(true);
            readNbt = nbtToolsClass.getDeclaredMethod("a", DataInput.class, Integer.TYPE, nbtReadLimiterClass);
            readNbt.setAccessible(true);
        } catch (NoSuchMethodException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to find writeNbt or readNbt method. Are you sure we support this Minecraft version?", e);
            return false;
        }

        return true;
    }

    @Override
    public String encode(Object[] craftItemStacks) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        Object nbtTagList = nbtTagListClass.newInstance();
        Method nbtTagListSizeMethod = nbtTagListClass.getMethod("size");
        Method nbtTagListAddMethod = nbtTagListClass.getMethod("c", int.class, nbtBaseClass);
        Method itemStackSaveMethod = nbtItemStackClass.getMethod("b", nbtTagCompoundClass);

        for (int i = 0; i < craftItemStacks.length; ++i) {
            Object nbtTagCompound = nbtTagCompoundClass.newInstance();
            Object itemStack = nbtItemStackClass.cast(craftItemStacks[i]);
            if (itemStack != null) {
                itemStackSaveMethod.invoke(itemStack, nbtTagCompound);
            }

            int size = (int) nbtTagListSizeMethod.invoke(nbtTagList);
            nbtTagListAddMethod.invoke(nbtTagList, size, nbtTagCompound);
        }

        writeNbt.invoke(null, nbtTagList, dataOutputStream);
        return new String(Base64.getEncoder().encode(byteArrayOutputStream.toByteArray()));
    }

    @Override
    public Object[] decode(String encoded) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(encoded));
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        Object nbtReadLimiter = nbtReadLimiterClass.getConstructor(long.class).newInstance(Long.MAX_VALUE);
        Object readInvoke = readNbt.invoke(null, dataInputStream, 0, nbtReadLimiter);

        Object nbtTagList = nbtTagListClass.cast(readInvoke);
        Method nbtTagListSizeMethod = nbtTagListClass.getMethod("size");
        Method nbtTagListGetMethod = nbtTagListClass.getMethod("k", int.class);
        int nbtTagListSize = (int) nbtTagListSizeMethod.invoke(nbtTagList);

        Method nbtTagCompoundIsEmptyMethod = nbtTagCompoundClass.getMethod("g");
        Object items = Array.newInstance(nbtItemStackClass, nbtTagListSize);

        Constructor<?> nbtItemStackConstructor = nbtItemStackClass.getDeclaredConstructor(nbtTagCompoundClass);
        nbtItemStackConstructor.setAccessible(true);

        for (int i = 0; i < nbtTagListSize; ++i) {
            Object nbtTagCompound = nbtTagListGetMethod.invoke(nbtTagList, i);
            boolean isEmpty = (boolean) nbtTagCompoundIsEmptyMethod.invoke(nbtTagCompound);
            if (!isEmpty) {
                Array.set(items, i, nbtItemStackConstructor.newInstance(nbtTagCompound));
            }
        }

        return (Object[]) items;
    }
}
