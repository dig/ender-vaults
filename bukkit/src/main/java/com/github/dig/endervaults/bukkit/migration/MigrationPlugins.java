package com.github.dig.endervaults.bukkit.migration;

import com.github.dig.endervaults.api.migration.Migrator;

public enum MigrationPlugins {

    PLAYERVAULTSX(new PVXMigrator()),

    ENDERCONTAINERS(new ECMigrator())

    ;

    private Migrator migrator;

    MigrationPlugins(Migrator migrator) {
        this.migrator = migrator;
    }

    public Migrator get() {
        return migrator;
    }
}
