package com.toyknight.aeii.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Created by toyknight on 4/4/2015.
 */
public class FileProvider {

    private FileProvider() {
    }

    public static FileHandle getAssetsFile(String path, Platform platform) {
        switch (platform) {
            case Android:
                return Gdx.files.internal(path);
            case iOS:
                return Gdx.files.internal(path);
            case Desktop:
            default:
                return Gdx.files.local("assets/" + path);
        }
    }

}
