package com.toyknight.aeii.utils;

/**
 * Created by toyknight on 4/4/2015.
 */
public enum Platform {

    Desktop, Android, iOS;

    public static boolean isMobileDevice(Platform platform) {
        return platform == Android || platform == iOS;
    }

}
