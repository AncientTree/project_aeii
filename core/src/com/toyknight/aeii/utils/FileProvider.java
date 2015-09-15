package com.toyknight.aeii.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;

/**
 * Created by toyknight on 4/4/2015.
 */
public class FileProvider {

    private static Platform platform = Platform.Desktop;

    private static final String user_home = System.getProperty("user.home") + "/aeii/";

    private FileProvider() {
    }

    public static void setPlatform(Platform platform) {
        FileProvider.platform = platform;
    }

    public static FileHandle getAssetsFile(String path) {
        switch (platform) {
            case Android:
            case iOS:
            case Desktop:
                return Gdx.files.internal(path);
            default:
                return Gdx.files.internal(path);
        }
    }

    public static FileHandle getUserFile(String path) {
        switch (platform) {
            case Android:
            case iOS:
                return Gdx.files.local("aeii/" + path);
            case Desktop:
            default:
                return Gdx.files.absolute(user_home + path);
        }
    }

    public static FileHandle getUserDir(String path) {
        FileHandle dir;
        switch (platform) {
            case Android:
            case iOS:
                dir = Gdx.files.local("aeii/" + path);
                break;
            case Desktop:
            default:
                dir = Gdx.files.absolute(user_home + path);
        }
        if (dir.exists() && dir.isDirectory()) {
            return dir;
        } else {
            dir.mkdirs();
            return dir;
        }
    }

    public static FileHandle getLanguageFile() {
        String locale = java.util.Locale.getDefault().toString();
        FileHandle language_file = getAssetsFile("lang/" + locale + ".dat");
        if (language_file.exists() && !language_file.isDirectory()) {
            return language_file;
        } else {
            return getAssetsFile("lang/en_US.dat");
        }
    }

}
