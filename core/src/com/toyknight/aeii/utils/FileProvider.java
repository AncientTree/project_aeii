package com.toyknight.aeii.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * @author toyknight 4/4/2015.
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
        return Gdx.files.internal(path);
    }

    public static FileHandle getUserFile(String path) {
        FileHandle file;
        switch (platform) {
            case Android:
            case iOS:
                file = Gdx.files.local("aeii/" + path);
                break;
            case Desktop:
            default:
                file = Gdx.files.absolute(user_home + path);
        }
        validateDirectory(file.parent());
        return file;
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
        validateDirectory(dir);
        return dir;
    }

    public static FileHandle getSaveFile(String filename) {
        FileHandle save_dir = getUserDir("save");
        validateDirectory(save_dir);
        FileHandle file;
        switch (platform) {
            case Android:
            case iOS:
                file = Gdx.files.local("aeii/save/" + filename);
                break;
            case Desktop:
            default:
                file = Gdx.files.absolute(user_home + "save/" + filename);
        }
        return file;
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

    public static FileHandle getUIDefaultFont() {
        String filename = Language.getFontFilename();
        return getAssetsFile("fonts/" + filename);
    }

    private static void validateDirectory(FileHandle directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            directory.mkdirs();
        }
    }

}
