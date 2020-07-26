package com.github.dig.endervaults.nms;

public interface VaultNMS {

    String encode(Object[] craftItemStacks);

    Object[] decode(String encoded);

    default Class<?> getItemStackClass() throws ClassNotFoundException {
        MinecraftVersion minecraftVersion = NMSProvider.getVersion();
        return Class.forName("net.minecraft.server." + minecraftVersion.toString() + ".ItemStack");
    }
}
