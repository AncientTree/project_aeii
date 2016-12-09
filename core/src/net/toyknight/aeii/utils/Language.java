package net.toyknight.aeii.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;
import net.toyknight.aeii.GameException;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Majirefy 5/22/15.
 */
public class Language {

    private static String locale;

    private static ObjectMap<String, String> languageMap = new ObjectMap<String, String>();

    public static void initialize() throws GameException {
        try {
            createLocale();
            FileHandle languageFile = FileProvider.getLanguageFile(locale);
            InputStreamReader reader = new InputStreamReader(languageFile.read(), "UTF8");
            PropertiesUtils.load(languageMap, reader);
        } catch (IOException ex) {
            throw new GameException(ex.getClass() + ": " + ex.getMessage());
        }
    }

    private static void createLocale() {
        locale = java.util.Locale.getDefault().toString();
        if (locale.startsWith("en_")) {
            locale = "en_US";
            return;
        }
        if (locale.startsWith("ru_")) {
            locale = "ru_RU";
            return;
        }
        if (locale.startsWith("pt_")) {
            locale = "pt_BR";
            return;
        }
        if (locale.equals("zh_SG")) {
            locale = "zh_CN";
        }
    }

    public static String getLocale() {
        return locale;
    }

    public static String getFontFilename() {
        return languageMap.get("FONT", "en_US.ttf");
    }

    public static String getUnitName(int index) {
        return languageMap.get("UNIT_NAME_" + index);
    }

    public static String getUnitGuide(int index) {
        return languageMap.get("UNIT_GUIDE_" + index);
    }

    public static String getUnitDescription(int index) {
        return languageMap.get("UNIT_DESCRIPTION_" + index);
    }

    public static String getAbilityName(int ability) {
        return languageMap.get("ABILITY_NAME_" + ability);
    }

    public static String getAbilityDescription(int ability) {
        return languageMap.get("ABILITY_DESCRIPTION_" + ability);
    }

    public static String getStatusName(int status) {
        return languageMap.get("STATUS_NAME_" + status);
    }

    public static String getStatusDescription(int status) {
        return languageMap.get("STATUS_DESCRIPTION_" + status);
    }

    public static String getText(String key) {
        return languageMap.get(key, "");
    }

    public static String createTextCharset(String default_chars) {
        ObjectMap.Values<String> values = languageMap.values();
        String language_chars = "";
        for (String text : values) {
            language_chars += text;
        }
        String additional_chars = FileProvider.getAssetsFile("lang/additional_chars.dat").readString("UTF8");
        return removeDuplicate(default_chars + language_chars + additional_chars);
    }

    public static String createTitleCharset(String default_chars) {
        String language_chars = "";
        for (String key : languageMap.keys()) {
            if (key.startsWith("_TITLE")) {
                language_chars += languageMap.get(key);
            }
        }
        return removeDuplicate(default_chars + language_chars);
    }

    public static String removeDuplicate(String str) {
        char[] temp = str.toCharArray();
        int length = temp.length;
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                if (temp[i] == temp[j]) {
                    int test = j;
                    for (int k = j + 1; k < length; k++) {
                        temp[test] = temp[k];
                        test++;
                    }
                    length--;
                    j--;
                }
            }
        }
        return String.copyValueOf(temp).substring(0, length);
    }

}
