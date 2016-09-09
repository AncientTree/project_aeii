package net.toyknight.aeii.utils;

/**
 * @author toyknight 4/4/2015.
 */
public enum Platform {

    Desktop, Android, iOS;

    public boolean isMobile() {
        return this == Android || this == iOS;
    }

    public boolean isDesktop() {
        return this == Desktop;
    }

}
