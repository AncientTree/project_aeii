package com.toyknight.aeii.net.server;

import com.esotericsoftware.kryonet.Connection;

/**
 * @author toyknight 10/27/2015.
 */
public class PlayerService {

    private final Connection connection;

    private boolean authenticated;

    private String address;
    private String username;

    public PlayerService(Connection connection) {
        this.connection = connection;
        authenticated = false;
        address = connection.getRemoteAddressTCP().getAddress().toString();
    }

    public Connection getConnection() {
        return connection;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getAddress() {
        return address;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

}
