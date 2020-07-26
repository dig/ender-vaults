package com.github.dig.endervaults.api.nms;

import com.github.dig.endervaults.api.PluginProvider;

public interface VaultNMS {

    String encode(Object[] craftItemStacks);

    Object[] decode(String encoded);

    default Class<?> getItemStackClass() throws ClassNotFoundException {
        MinecraftVersion minecraftVersion = PluginProvider.getPlugin().getServerVersion();
        return Class.forName("net.minecraft.server." + minecraftVersion.toString() + ".ItemStack");
    }
}
