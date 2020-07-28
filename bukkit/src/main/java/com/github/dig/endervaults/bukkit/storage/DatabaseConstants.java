package com.github.dig.endervaults.bukkit.storage;

public class DatabaseConstants {

    public static final String SQL_CREATE_TABLE_VAULT = "CREATE TABLE IF NOT EXISTS `%s` ( `id` VARCHAR(36) NOT NULL , `owner_uuid` VARCHAR(36) NOT NULL , `size` INT(8) NOT NULL , `contents` TEXT NOT NULL )";
    public static final String SQL_CREATE_TABLE_VAULT_METADATA = "CREATE TABLE IF NOT EXISTS `%s` ( `id` VARCHAR(36) NOT NULL , `owner_uuid` VARCHAR(36) NOT NULL , `name` VARCHAR(16) NOT NULL , `value` TEXT NOT NULL )";

    public static final String SQL_SELECT_VAULT_BY_ID_AND_OWNER = "SELECT * FROM `%s` WHERE id = ? AND owner_uuid = ?";
    public static final String SQL_SELECT_VAULT_BY_OWNER = "SELECT * FROM `%s` WHERE owner_uuid = ?";

}
