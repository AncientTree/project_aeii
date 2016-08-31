package net.toyknight.aeii.server;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author toyknight 8/22/2016.
 */
public class ServerConfiguration {

    private final File configuration_file = new File("server.cfg");

    private final ObjectMap<String, String> configuration = new ObjectMap<String, String>();

    private int port;

    private boolean map_manager_enabled;

    private String admin_token;

    private String database_host;

    private String database_name;

    private String database_username;

    private String database_password;

    public void initialize() throws IOException {
        FileReader configuration_reader = new FileReader(configuration_file);
        PropertiesUtils.load(configuration, configuration_reader);
        //parse the configuration
        port = Integer.parseInt(configuration.get("PORT", "5438"));
        admin_token = configuration.get("ADMIN_TOKEN", "123456");
        map_manager_enabled = Boolean.parseBoolean(configuration.get("MAP_MANAGER_ENABLED", "false"));
        database_host = configuration.get("DATABASE_HOST", "127.0.0.1");
        database_name = configuration.get("DATABASE_NAME", "aeii");
        database_username = configuration.get("DATABASE_USERNAME", "undefined");
        database_password = configuration.get("DATABASE_PASSWORD", "123456");
    }

    public int getPort() {
        return port;
    }

    public String getAdministratorToken() {
        return admin_token;
    }

    public boolean isMapManagerEnabled() {
        return map_manager_enabled;
    }

    public String getDatabaseHost() {
        return database_host;
    }

    public String getDatabaseName() {
        return database_name;
    }

    public String getDatabaseUsername() {
        return database_username;
    }

    public String getDatabasePassword() {
        return database_password;
    }

}
