package com.github.dig.endervaults.bukkit.lang;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.exception.LanguageMissingException;
import com.github.dig.endervaults.api.file.DataFile;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.lang.Language;
import lombok.extern.java.Log;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;
import java.util.logging.Level;

@Log
public class BukkitLanguage implements Language {

    private final DataFile langFile = PluginProvider.getPlugin().getLangFile();

    @Override
    public String get(Lang lang) {
        String text;
        try {
            text = getText(lang.getKey());
        } catch (LanguageMissingException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to get language text for " + lang.toString() + ", please delete lang.yml and it should generate properly.", e);
            return "";
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @Override
    public String get(Lang lang, Map<String, Object> placeholders) {
        String text;
        try {
            text = getText(lang.getKey());
        } catch (LanguageMissingException e) {
            log.log(Level.SEVERE, "[EnderVaults] Unable to get language text for " + lang.toString() + ", please delete lang.yml and it should generate properly.", e);
            return "";
        }

        for (String key : placeholders.keySet()) {
            Object value = placeholders.get(key);
            text = text.replaceAll("%" + key, String.valueOf(value));
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String getText(String key) throws LanguageMissingException {
        FileConfiguration configuration = (FileConfiguration) langFile.getConfiguration();
        String text = configuration.getString(key);
        if (text == null) {
            throw new LanguageMissingException("Missing language key " + key + " from lang.yml.");
        }
        return text;
    }
}
