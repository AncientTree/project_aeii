package net.toyknight.aeii.system;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;
import net.toyknight.aeii.GameException;
import net.toyknight.aeii.utils.FileProvider;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author toyknight 12/9/2016.
 */
public class Language {

    private String locale;

    private ObjectMap<String, String> languageMap = new ObjectMap<String, String>();

    protected Language() {
    }

    public void initialize() throws GameException {
        try {
            createLocale();
            FileHandle languageFile = FileProvider.getLanguageFile(locale);
            InputStreamReader reader = new InputStreamReader(languageFile.read(), "UTF8");
            PropertiesUtils.load(languageMap, reader);
        } catch (IOException ex) {
            throw new GameException(ex.getClass() + ": " + ex.getMessage());
        }
    }

    private void createLocale() {
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

    public String getLocale() {
        return locale;
    }

    public String getFontFilename() {
        return languageMap.get("FONT", "en_US.ttf");
    }

    public String getUnitName(int index) {
        return languageMap.get("UNIT_NAME_" + index);
    }

    public String getUnitGuide(int index) {
        return languageMap.get("UNIT_GUIDE_" + index);
    }

    public String getUnitDescription(int index) {
        return languageMap.get("UNIT_DESCRIPTION_" + index);
    }

    public String getAbilityName(int ability) {
        return languageMap.get("ABILITY_NAME_" + ability);
    }

    public String getAbilityDescription(int ability) {
        return languageMap.get("ABILITY_DESCRIPTION_" + ability);
    }

    public String getStatusName(int status) {
        return languageMap.get("STATUS_NAME_" + status);
    }

    public String getStatusDescription(int status) {
        return languageMap.get("STATUS_DESCRIPTION_" + status);
    }

    public String getText(String key) {
        return languageMap.get(key, "");
    }

    public String createTextCharset(String default_chars) {
        ObjectMap.Values<String> values = languageMap.values();
        String language_chars = "";
        for (String text : values) {
            language_chars += text;
        }
        String additional_chars = FileProvider.getAssetsFile("lang/additional_chars.dat").readString("UTF8");
        return removeDuplicate(default_chars + language_chars + additional_chars);
    }

    public String createTitleCharset(String default_chars) {
        String language_chars = "";
        for (String key : languageMap.keys()) {
            if (key.startsWith("_TITLE")) {
                language_chars += languageMap.get(key);
            }
        }
        return removeDuplicate(default_chars + language_chars);
    }

    public String removeDuplicate(String str) {
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
