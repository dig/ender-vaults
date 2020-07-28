package com.github.dig.endervaults.api.vault;

import java.util.Map;
import java.util.UUID;

public interface Vault {

    UUID getId();

    UUID getOwner();

    int getSize();

    int getFreeSize();

    Map<String, Object> getMetadata();

}
