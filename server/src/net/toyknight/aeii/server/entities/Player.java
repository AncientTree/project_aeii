package net.toyknight.aeii.server.entities;

import com.esotericsoftware.kryonet.Connection;
import net.toyknight.aeii.network.entity.PlayerSnapshot;

/**
 * @author toyknight 10/27/2015.
 */
public class Player {

    private final Object SENDING_LOCK = new Object();

    private final Connection connection;

    private boolean authenticated;

    private String address;
    private String username;

    private long room_number;

    public Player(Connection connection) {
        this.connection = connection;
        authenticated = false;
        room_number = -1;
        address = connection.getRemoteAddressTCP().getAddress().toString();
    }

    public int getID() {
        return getConnection().getID();
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

    public void setRoomID(long number) {
        room_number = number;
    }

    public long getRoomID() {
        return room_number;
    }

    public PlayerSnapshot createSnapshot() {
        return new PlayerSnapshot(getID(), getUsername());
    }

    public void sendTCP(Object object) {
        synchronized (SENDING_LOCK) {
            if (getConnection().isConnected()) {
                getConnection().sendTCP(object);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s@%s", getUsername(), getAddress());
    }

}
