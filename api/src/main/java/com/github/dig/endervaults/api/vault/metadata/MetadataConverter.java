package com.github.dig.endervaults.api.vault.metadata;

public interface MetadataConverter<T> {

    T to(String value);

    String from(T value);

}
