package com.toyknight.aeii.network.server;

/**
 * @author toyknight 8/24/2015.
 */
public class ServerConfiguration {

    private final String address;
    private final int port;
    private final String name;

    public ServerConfiguration(String address, int port, String name) {
        this.address = address;
        this.port = port;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

}
