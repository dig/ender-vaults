package com.github.dig.endervaults.api.lang;

import java.util.Map;

public interface Language {

    String get(Lang lang);

    String get(Lang lang, Map<String, Object> placeholders);

}
