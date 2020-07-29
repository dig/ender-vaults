package com.github.dig.endervaults.api.migration;

public interface Migrator {

    boolean can();

    String response();

    void migrate();

}
