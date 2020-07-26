package com.github.dig.endervaults.api.util;

public interface VaultSerializable {

    String encode();

    void decode(String encoded);

}
