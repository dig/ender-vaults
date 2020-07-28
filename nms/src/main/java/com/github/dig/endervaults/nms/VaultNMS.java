package com.github.dig.endervaults.nms;

import java.lang.reflect.InvocationTargetException;

public interface VaultNMS {

    boolean init(MinecraftVersion version);

    String encode(Object[] craftItemStacks) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException;

    Object[] decode(String encoded) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException;

    default Class<?> getItemStackClass() throws ClassNotFoundException {
        MinecraftVersion minecraftVersion = NMSProvider.getVersion();
        return Class.forName("net.minecraft.server." + minecraftVersion.toString() + ".ItemStack");
    }
}
