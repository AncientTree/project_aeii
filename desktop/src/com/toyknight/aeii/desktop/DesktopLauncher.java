package com.toyknight.aeii.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.Platform;

public class DesktopLauncher implements Runnable {

    private final LwjglApplicationConfiguration config;
    private final AEIIApplication application;

    public DesktopLauncher(int ts, int width, int height, boolean fs, int fps) {
        config = new LwjglApplicationConfiguration();
        config.title = "Ancient Empires II 10 Years Anniversary";
        config.fullscreen = fs;
        config.width = width;
        config.height = height;
        if (!config.fullscreen) {
            config.resizable = false;
            config.useHDPI = true;
        }
        config.backgroundFPS = -1;
        config.foregroundFPS = fps;
        application = new AEIIApplication(Platform.Desktop, ts);
    }

    @Override
    public void run() {
        new LwjglApplication(application, config);
    }

    private static void showLaunchGuide() {
        System.err.println("Usage: <tile_size> <width> <height> <fullscreen> <fps>");
    }

    public static void main(String[] args) {
        if (args.length >= 5) {
            try {
                int ts = Integer.parseInt(args[0]);
                int width = Integer.parseInt(args[1]);
                int height = Integer.parseInt(args[2]);
                boolean fs = Boolean.parseBoolean(args[3]);
                int fps = Integer.parseInt(args[4]);
                new Thread(new DesktopLauncher(ts, width, height, fs, fps)).start();
            } catch (NumberFormatException ex) {
                showLaunchGuide();
            }
        } else {
            showLaunchGuide();
        }
    }

}
