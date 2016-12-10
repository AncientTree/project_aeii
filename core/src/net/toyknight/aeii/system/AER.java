package net.toyknight.aeii.system;

import net.toyknight.aeii.GameException;
import net.toyknight.aeii.utils.Platform;

/**
 * @author toyknight 12/9/2016.
 */
public class AER {

    public static int ts;
    public static Platform platform;

    public static Units units;
    public static Tiles tiles;

    public static Language lang;

    public static Audio audio;

    public static Font font;

    public static Resources resources;

    private AER() {
    }

    public static void initialize() throws GameException {
        initializeData();
        lang = new Language();
        lang.initialize();
        audio = new Audio();
        font = new Font(ts);
        resources = new Resources();
        resources.prepare(ts);
    }

    public static void initializeData() throws GameException {
        units = new Units();
        units.initialize();
        tiles = new Tiles();
        tiles.initialize();
    }

}
