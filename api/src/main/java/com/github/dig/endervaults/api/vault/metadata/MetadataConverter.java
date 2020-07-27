package com.github.dig.endervaults.api.vault.metadata;

public interface MetadataConverter<T> {

    T to(Object value);

    Object from(T value);

}
