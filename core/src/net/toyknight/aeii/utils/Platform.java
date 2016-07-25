package net.toyknight.aeii.utils;

/**
 * @author toyknight 4/4/2015.
 */
public enum Platform {

    Desktop, Android, iOS;

    public static boolean isMobile(Platform platform) {
        return platform == Android || platform == iOS;
    }

    public static boolean isDesktop(Platform platform) {
        return platform == Desktop;
    }

}
