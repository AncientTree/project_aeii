package net.toyknight.aeii.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import java.io.File;
import java.io.FileFilter;

/**
 * @author toyknight 4/4/2015.
 */
public class FileProvider {

    private static final SaveFileFilter save_file_filter = new SaveFileFilter();

    private static final String user_home = System.getProperty("user.home") + "/.aeii/";

    private static Platform platform = Platform.Desktop;

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
                file = Gdx.files.local(".aeii/" + path);
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
                dir = Gdx.files.local(".aeii/" + path);
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
                file = Gdx.files.local(".aeii/save/" + filename);
                break;
            case Desktop:
            default:
                file = Gdx.files.absolute(user_home + "save/" + filename);
        }
        return file;
    }

    public static Array<FileHandle> getSaveFiles() {
        FileHandle save_dir = FileProvider.getUserDir("save");
        return new Array<FileHandle>(save_dir.list(save_file_filter));
    }

    public static FileHandle getLanguageFile() {
        String locale = java.util.Locale.getDefault().toString();
        FileHandle language_file = getAssetsFile("lang/" + locale + ".dat");
        if (language_file.exists() && !language_file.isDirectory()) {
            return language_file;
        } else {
            return getAssetsFile("lang/en_US.dat");
        }
//        return getAssetsFile("lang/zh_CN.dat");
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

    private static class SaveFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            if (file.exists() && !file.isDirectory()) {
                String filename = file.getName();
                return filename.endsWith(".sav");
            } else {
                return false;
            }

        }

    }

}
