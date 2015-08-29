package com.toyknight.aeii.server.entity;

/**
 * Created by toyknight on 8/24/2015.
 */
public class Server {

    private final String address;
    private final int port;
    private final String name;

    public Server(String address, int port, String name) {
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
