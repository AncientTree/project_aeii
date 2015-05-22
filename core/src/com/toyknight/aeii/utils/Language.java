package com.toyknight.aeii.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by Majirefy on 5/22/15.
 */
public class Language {
    private static ObjectMap<String, String> languageMap = new ObjectMap<String, String>();

    public static void initialize() throws IOException {
        FileHandle languageFile = FileProvider.getAssetsFile("lang/lang.dat");
        Reader languageReader = languageFile.reader();
        PropertiesUtils.load(languageMap, languageReader);
    }

    public static String getText(String key) {
        return languageMap.get(key);
    }
}
