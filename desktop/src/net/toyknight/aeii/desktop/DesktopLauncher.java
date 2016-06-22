package net.toyknight.aeii.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.network.server.GameServer;
import net.toyknight.aeii.utils.Platform;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URISyntaxException;

public class DesktopLauncher {

    private final Resolution[] RESOLUTION_PRESET =
            new Resolution[]{new Resolution(848, 480), new Resolution(1280, 720), new Resolution(1440, 900)};

    private final Integer[] FPS_PRESET = new Integer[]{30, 40, 50, 60};

    private ObjectMap<String, String> language;

    private ObjectMap<String, String> configuration;

    private JFrame launcher_window;

    private JComboBox<Resolution> resolution_list;

    private JComboBox<Integer> fps_list;

    private JCheckBox switch_fullscreen;

    private void validateApplicationHome() throws IOException {
        File application_home = new File(System.getProperty("user.home") + "/.aeii/");
        if (!application_home.exists() && !application_home.mkdirs()) {
            throw new IOException("Cannot create application directory!");
        }
    }

    private void loadLanguage() throws IOException, URISyntaxException {
        String locale = java.util.Locale.getDefault().toString();
        InputStream language_in = getClass().getResourceAsStream("/launcher/" + locale + ".dat");
        InputStream default_language_in = getClass().getResourceAsStream("/launcher/en_US.dat");
        language_in = language_in == null ? default_language_in : language_in;
        InputStreamReader reader = new InputStreamReader(language_in, "UTF8");
        language = new ObjectMap<String, String>();
        PropertiesUtils.load(language, reader);
    }

    private void loadConfiguration() throws IOException {
        File launcher_config_file = new File(System.getProperty("user.home") + "/.aeii/launcher.config");
        if (launcher_config_file.exists()) {
            FileReader reader = new FileReader(launcher_config_file);
            configuration = new ObjectMap<String, String>();
            PropertiesUtils.load(configuration, reader);
        } else {
            if (launcher_config_file.createNewFile()) {
                configuration = createDefaultConfiguration();
                saveConfiguration();
            } else {
                throw new IOException("Cannot create launcher configuration file!");
            }
        }
    }

    private void saveConfiguration() throws IOException {
        File launcher_config_file = new File(System.getProperty("user.home") + "/.aeii/launcher.config");
        FileWriter writer = new FileWriter(launcher_config_file);
        PropertiesUtils.store(configuration, writer, "aeii launcher configuration file");
    }

    private ObjectMap<String, String> createDefaultConfiguration() {
        ObjectMap<String, String> config = new ObjectMap<String, String>();
        config.put("WIDTH", Integer.toString(848));
        config.put("HEIGHT", Integer.toString(480));
        config.put("FULLSCREEN", Boolean.toString(false));
        config.put("FPS", Integer.toString(60));
        return config;
    }

    public ObjectMap<String, String> getLanguage() {
        return language;
    }

    public ObjectMap<String, String> getConfiguration() {
        return configuration;
    }

    private JFrame createWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ignored) {
        } catch (InstantiationException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (UnsupportedLookAndFeelException ignored) {
        }
        JFrame window = new JFrame(getLanguage().get("LB_LAUNCHER"));
        window.getContentPane().setPreferredSize(new Dimension(240, 150));

        window.getContentPane().setLayout(new FlowLayout());

        JLabel label_resolution = new JLabel(getLanguage().get("LB_RESOLUTION"));
        label_resolution.setHorizontalAlignment(SwingConstants.RIGHT);
        label_resolution.setPreferredSize(new Dimension(60, 32));
        window.getContentPane().add(label_resolution);

        resolution_list = new JComboBox<Resolution>(RESOLUTION_PRESET);
        ((JLabel) resolution_list.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        resolution_list.setPreferredSize(new Dimension(150, 32));
        resolution_list.setFocusable(false);
        window.getContentPane().add(resolution_list);

        JLabel label_fps = new JLabel(getLanguage().get("LB_FPS"));
        label_fps.setHorizontalAlignment(SwingConstants.RIGHT);
        label_fps.setPreferredSize(new Dimension(60, 32));
        window.getContentPane().add(label_fps);

        fps_list = new JComboBox<Integer>(FPS_PRESET);
        ((JLabel) fps_list.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        fps_list.setPreferredSize(new Dimension(150, 32));
        fps_list.setFocusable(false);
        window.getContentPane().add(fps_list);

        switch_fullscreen = new JCheckBox(getLanguage().get("LB_FULLSCREEN"));
        switch_fullscreen.setFocusable(false);
        switch_fullscreen.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                resolution_list.setEnabled(!switch_fullscreen.isSelected());
            }
        });
        window.getContentPane().add(switch_fullscreen);

        JButton btn_launch = new JButton(getLanguage().get("LB_LAUNCH"));
        btn_launch.setPreferredSize(new Dimension(220, 36));
        btn_launch.setFocusable(false);
        btn_launch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tryLaunchGame();
            }
        });
        window.getContentPane().add(btn_launch);

        window.setResizable(false);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        return window;
    }

    private void update() {
        int width = Integer.parseInt(getConfiguration().get("WIDTH"));
        int height = Integer.parseInt(getConfiguration().get("HEIGHT"));
        for (int i = 0; i < RESOLUTION_PRESET.length; i++) {
            if (RESOLUTION_PRESET[i].equals(width, height)) {
                resolution_list.setSelectedIndex(i);
                break;
            }
        }
        int fps = Integer.parseInt(getConfiguration().get("FPS"));
        for (int i = 0; i < FPS_PRESET.length; i++) {
            if (FPS_PRESET[i] == fps) {
                fps_list.setSelectedIndex(i);
                break;
            }
        }
        boolean fullscreen = Boolean.parseBoolean(getConfiguration().get("FULLSCREEN"));
        switch_fullscreen.setSelected(fullscreen);
    }

    private void tryLaunchGame() {
        launcher_window.dispose();

        int width;
        int height;
        Integer fps = (Integer) fps_list.getSelectedItem();
        boolean fullscreen = switch_fullscreen.isSelected();

        if (fullscreen) {
            Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
            width = screen_size.width;
            height = screen_size.height;
        } else {
            Resolution resolution = (Resolution) resolution_list.getSelectedItem();
            width = resolution.getWidth();
            height = resolution.getHeight();
        }

        configuration.put("WIDTH", Integer.toString(width));
        configuration.put("HEIGHT", Integer.toString(height));
        configuration.put("FULLSCREEN", Boolean.toString(fullscreen));
        configuration.put("FPS", Integer.toString(fps));
        try {
            saveConfiguration();
        } catch (IOException ignored) {
        }

        launch(height / 10, width, height, fullscreen, fps);
    }

    public void launch() {
        try {
            validateApplicationHome();
            loadLanguage();
            loadConfiguration();
            launcher_window = createWindow();
            update();
            launcher_window.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void launch(int ts, int width, int height, boolean fs, int fps) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.addIcon("desktop_icon_large.png", Files.FileType.Internal);
        config.addIcon("desktop_icon_medium.png", Files.FileType.Internal);
        config.addIcon("desktop_icon_small.png", Files.FileType.Internal);
        config.title = getLanguage().get("LB_TITLE");
        config.fullscreen = fs;
        config.width = width;
        config.height = height;
        if (!config.fullscreen) {
            config.resizable = false;
            config.useHDPI = true;
        }
        config.foregroundFPS = fps;
        GameContext application = new GameContext(Platform.Desktop, ts);
        new LwjglApplication(application, config);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("-server")) {
                new GameServer().start();
            } else {
                if (args.length >= 5) {
                    try {
                        int ts = Integer.parseInt(args[0]);
                        int width = Integer.parseInt(args[1]);
                        int height = Integer.parseInt(args[2]);
                        boolean fs = Boolean.parseBoolean(args[3]);
                        int fps = Integer.parseInt(args[4]);
                        new DesktopLauncher().launch(ts, width, height, fs, fps);
                    } catch (NumberFormatException ex) {
                        new DesktopLauncher().launch();
                    }
                } else {
                    new DesktopLauncher().launch();
                }
            }
        } else {
            new DesktopLauncher().launch();
        }
    }

}
