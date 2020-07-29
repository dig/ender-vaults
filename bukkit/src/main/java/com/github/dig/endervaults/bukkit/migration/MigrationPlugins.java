package com.github.dig.endervaults.bukkit.migration;

import com.github.dig.endervaults.api.migration.Migrator;

import java.util.Optional;

public enum MigrationPlugins {

    PLAYERVAULTSX;

    public Optional<Migrator> get() {
        switch (this) {
            case PLAYERVAULTSX:
                return Optional.of(new PVXMigrator());
        }
        return Optional.empty();
    }
}
