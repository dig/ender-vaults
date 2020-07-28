package com.github.dig.endervaults.bukkit.permission;

import com.github.dig.endervaults.api.permission.UserPermission;
import org.bukkit.entity.Player;

public class BukkitUserPermission implements UserPermission<Player> {

    @Override
    public int getTotalVaults(Player player) {
        int count = 0;
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            if (canUseVault(player, i)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean canUseVault(Player player, int order) {
        return player.hasPermission("endervaults.vault." + order);
    }

    @Override
    public boolean canUseVaultCommand(Player player) {
        return player.hasPermission("endervaults.command.use");
    }

    @Override
    public boolean isVaultAdmin(Player player) {
        return player.hasPermission("endervaults.admin");
    }

    @Override
    public boolean canBypassBlacklist(Player player) {
        return player.hasPermission("endervaults.bypass.blacklist");
    }

    @Override
    public boolean canReload(Player player) {
        return player.hasPermission("endervaults.admin.reload");
    }

    @Override
    public boolean canSelectIcon(Player player) {
        return player.hasPermission("endervaults.select.icon");
    }
}
