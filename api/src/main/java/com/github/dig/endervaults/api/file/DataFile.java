package com.github.dig.endervaults.api.file;

import java.io.File;

public interface DataFile {

    File getFile();

    void load();

    void save();

}
