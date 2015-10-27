package com.toyknight.aeii.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.server.AEIIServer;
import com.toyknight.aeii.utils.Platform;

public class DesktopLauncher implements Runnable {

    private final LwjglApplicationConfiguration config;
    private final GameContext application;

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
        config.foregroundFPS = fps;
        application = new GameContext(Platform.Desktop, ts);
    }

    @Override
    public void run() {
        new LwjglApplication(application, config);
    }

    public static void main(String[] args) {
        if (args.length >= 1) {
            if (args[0].equals("-server")) {
                new AEIIServer().start();
            } else {
                if (args.length >= 5) {
                    int ts = Integer.parseInt(args[0]);
                    int width = Integer.parseInt(args[1]);
                    int height = Integer.parseInt(args[2]);
                    boolean fs = Boolean.parseBoolean(args[3]);
                    int fps = Integer.parseInt(args[4]);
                    new Thread(new DesktopLauncher(ts, width, height, fs, fps)).start();
                }
            }
        }
    }

}
