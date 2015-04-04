package com.toyknight.aeii.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.toyknight.aeii.AEIIApplication;

import java.awt.*;

public class DesktopLauncher implements Runnable {

    private final LwjglApplicationConfiguration config;
    private final AEIIApplication application;

    public DesktopLauncher(int ts, int width, int height, boolean fs) {
        config = new LwjglApplicationConfiguration();
        config.title = "Ancient Empires II 10 Years Anniversary";
        config.fullscreen = fs;
        config.width = width;
        config.height = height;
        config.useHDPI = true;
        application = new AEIIApplication(ts);
    }

    @Override
    public void run() {
        new LwjglApplication(application, config);
    }

    public static void main(String[] args) {
        if (args.length >= 4) {
            try {
                int ts = Integer.parseInt(args[0]);
                int width = Integer.parseInt(args[1]);
                int height = Integer.parseInt(args[2]);
                boolean fs = Boolean.parseBoolean(args[3]);
                EventQueue.invokeLater(new DesktopLauncher(ts, width, height, fs));
            } catch (NumberFormatException ex) {
                System.err.println(ex.getClass().toString() + ": " + ex.getMessage());
            }
        } else {
            System.err.println("Usage: <tile_size> <width> <height> <fullscreen>");
        }
    }

}
