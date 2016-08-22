package net.toyknight.aeii.server;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.InputMismatchException;

/**
 * @author toyknight 8/22/2016.
 */
public class ServerConfiguration {

    private int port;

    private boolean map_manager_enabled;

    private String admin_token;


    public void initialize() throws IOException, InputMismatchException {
        File configuration_file = new File("server.cfg");
        FileReader configuration_reader = new FileReader(configuration_file);
        ObjectMap<String, String> configuration = new ObjectMap<String, String>();
        PropertiesUtils.load(configuration, configuration_reader);
        //parse the configuration
        port = Integer.parseInt(configuration.get("PORT", "5438"));
        map_manager_enabled = Boolean.parseBoolean(configuration.get("MAP_ENABLED", "false"));
        admin_token = configuration.get("ADMIN_TOKEN", "123456");
    }

    public int getPort() {
        return port;
    }

    public boolean isMapManagerEnabled() {
        return map_manager_enabled;
    }

    public String getAdministratorToken() {
        return admin_token;
    }

}
