package com.github.dig.endervaults.bukkit.storage;

public class DatabaseConstants {

    public static final String SQL_CREATE_TABLE_VAULT = "CREATE TABLE IF NOT EXISTS `%s` ( `id` VARCHAR(36) NOT NULL , `owner_uuid` VARCHAR(36) NOT NULL , `size` INT(8) NOT NULL , `contents` LONGTEXT NOT NULL , PRIMARY KEY (`id`, `owner_uuid`) )";
    public static final String SQL_CREATE_TABLE_VAULT_METADATA = "CREATE TABLE IF NOT EXISTS `%s` ( `id` VARCHAR(36) NOT NULL , `owner_uuid` VARCHAR(36) NOT NULL , `name` VARCHAR(16) NOT NULL , `value` TEXT NOT NULL , PRIMARY KEY (`id`, `owner_uuid`, `name`) )";

    public static final String SQL_SELECT_VAULT_BY_ID_AND_OWNER = "SELECT * FROM `%s` WHERE `id` = ? AND `owner_uuid` = ?";
    public static final String SQL_SELECT_VAULT_BY_OWNER = "SELECT * FROM `%s` WHERE `owner_uuid` = ?";
    public static final String SQL_INSERT_VAULT = "INSERT INTO `%s`(`id`, `owner_uuid`, `size`, `contents`) VALUES (?, ?, ?, ?)";
    public static final String SQL_UPDATE_VAULT_BY_ID_AND_OWNER = "UPDATE `%s` SET `size` = ?, `contents` = ? WHERE `id` = ? AND `owner_uuid` = ?";

    public static final String SQL_SELECT_VAULT_METADATA_BY_ID_AND_OWNER = "SELECT * FROM `%s` WHERE `id` = ? AND `owner_uuid` = ?";
    public static final String SQL_SELECT_VAULT_METADATA_BY_ID_AND_OWNER_AND_KEY = "SELECT * FROM `%s` WHERE `id` = ? AND `owner_uuid` = ? AND `name` = ?";
    public static final String SQL_INSERT_VAULT_METADATA = "INSERT INTO `%s`(`id`, `owner_uuid`, `name`, `value`) VALUES (?, ?, ?, ?)";
    public static final String SQL_UPDATE_VAULT_METADATA_BY_ID_AND_OWNER_AND_KEY = "UPDATE `%s` SET `value` = ? WHERE `id` = ? AND `owner_uuid` = ? AND `name` = ?";

}
