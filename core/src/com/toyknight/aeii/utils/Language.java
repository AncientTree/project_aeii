package com.toyknight.aeii.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;
import com.toyknight.aeii.AEIIException;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by Majirefy on 5/22/15.
 */
public class Language {

    private static ObjectMap<String, String> languageMap = new ObjectMap();

    public static void init() throws AEIIException {
        try {
            FileHandle languageFile = FileProvider.getAssetsFile("lang/lang.dat");
            Reader languageReader = languageFile.reader();
            PropertiesUtils.load(languageMap, languageReader);
        } catch (IOException ex) {
            throw new AEIIException(ex.getClass() + ": " + ex.getMessage());
        }
    }

    public static String getUnitName(String package_name, int index) {
        if (package_name.equals("default")) {
            return languageMap.get("UNIT_NAME_" + index);
        } else {
            return "";
        }
    }

    public static String getText(String key) {
        return languageMap.get(key);
    }

    public static String createCharset() {
        ObjectMap.Values<String> values = languageMap.values();
        String charset = "";
        for (String text : values) {
            charset += text;
        }
        return removeDuplicate(charset + "0123456789" + "\"!`?'.,;:()[]{}<>|/@\\^$-%+=#_&~*");
    }

    private static String removeDuplicate(String s) {
        char[] temp = s.toCharArray();
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
