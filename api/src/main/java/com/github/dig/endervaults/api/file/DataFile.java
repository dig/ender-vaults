package com.github.dig.endervaults.api.file;

import java.io.File;

public interface DataFile<T> {

    T getConfiguration();

    File getFile();

    void load();

    void save();

}
