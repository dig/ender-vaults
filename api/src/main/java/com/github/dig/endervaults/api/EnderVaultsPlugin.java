package com.github.dig.endervaults.api;

import com.github.dig.endervaults.api.file.DataFile;
import com.github.dig.endervaults.api.lang.Language;
import com.github.dig.endervaults.api.storage.DataStorage;
import com.github.dig.endervaults.api.vault.VaultPersister;
import com.github.dig.endervaults.api.vault.metadata.VaultMetadataRegistry;
import com.github.dig.endervaults.nms.MinecraftVersion;
import com.github.dig.endervaults.api.vault.VaultRegistry;

public interface EnderVaultsPlugin {

    MinecraftVersion getVersion();

    DataFile getLangFile();

    DataFile getConfigFile();

    VaultRegistry getRegistry();

    Language getLanguage();

    DataStorage getDataStorage();

    VaultPersister getPersister();

    VaultMetadataRegistry getMetadataRegistry();
}
