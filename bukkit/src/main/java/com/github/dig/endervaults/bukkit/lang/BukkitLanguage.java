package com.github.dig.endervaults.bukkit.lang;

import com.github.dig.endervaults.api.PluginProvider;
import com.github.dig.endervaults.api.file.DataFile;
import com.github.dig.endervaults.api.lang.Lang;
import com.github.dig.endervaults.api.lang.Language;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public class BukkitLanguage implements Language {

    private final DataFile langFile = PluginProvider.getPlugin().getLangFile();

    @Override
    public String get(Lang lang, Map<String, Object> placeholders) {
        FileConfiguration configuration = (FileConfiguration) langFile.getConfiguration();
        String text = configuration.getString(lang.getKey());
        for (String key : placeholders.keySet()) {
            Object value = placeholders.get(key);
            text = text.replaceAll("%" + key, String.valueOf(value));
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
