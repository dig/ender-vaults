package com.github.dig.endervaults.api.permission;

public interface UserPermission<P> {

    int getTotalVaults(P player);

    boolean canUseVault(P player, int order);

    boolean canUseVaultCommand(P player);

    boolean isVaultAdmin(P player);

    boolean canBypassBlacklist(P player);

    boolean canReload(P player);

    boolean canSelectIcon(P player);

}
