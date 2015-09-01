package com.toyknight.aeii.server;

import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.net.NetworkManager;
import com.toyknight.aeii.net.Request;
import com.toyknight.aeii.server.entity.Room;
import com.toyknight.aeii.server.entity.RoomConfig;
import com.toyknight.aeii.server.entity.RoomSnapshot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * @author toyknight
 */
public class PlayerService extends Thread {

    private final Object INPUT_LOCK = new Object();
    private final Object OUTPUT_LOCK = new Object();

    private final AEIIServer context;
    private final Socket client;

    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;

    private String username = "undefined";
    private long room_number = -1;
    private boolean[] team_access;

    public PlayerService(AEIIServer context, Socket client, String name) throws IOException {
        super(context.getServiceGroup(), name);
        this.context = context;
        this.client = client;

        this.oos = new ObjectOutputStream(client.getOutputStream());
        this.ois = new ObjectInputStream(client.getInputStream());
        this.oos.flush();

        this.team_access = new boolean[4];
        Arrays.fill(team_access, false);
    }

    private AEIIServer getContext() {
        return context;
    }

    public void create() {

    }

    public String getClientAddress() {
        return client.getInetAddress().getHostAddress();
    }


    public String getUsername() {
        return username;
    }

    public void setRoomNumber(long room_number) {
        this.room_number = room_number;
    }

    public long getRoomNumber() {
        return room_number;
    }

    public void setTeamAccess(int team, boolean access) {
        team_access[team] = access;
    }

    public boolean[] getTeamAccess() {
        return team_access;
    }

    private void closeConnection() {
        try {
            client.close();
        } catch (IOException ex) {
            getContext().getLogger().log(Level.WARNING, ex.toString());
        }
    }

    public void notifyPlayerLeaving(String service_name, String username) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(NetworkManager.REQUEST);
            oos.writeInt(Request.PLAYER_LEAVING);
            oos.writeUTF(service_name);
            oos.writeUTF(username);
            oos.flush();
        }
    }

    private void respondOpenRooms() throws IOException {
        ArrayList<RoomSnapshot> snapshot = getContext().getOpenRoomSnapshot();
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(NetworkManager.RESPONSE);
            oos.writeObject(snapshot);
            oos.flush();
        }
        getContext().getLogger().log(
                Level.INFO,
                "Player {0}@{1} fetches room list",
                new Object[]{getUsername(), getClientAddress()});
    }

    private void respondJoinRoom(long room_number) throws IOException {
        RoomConfig config = getContext().onPlayerJoinRoom(getName(), room_number);
        synchronized (OUTPUT_LOCK) {
            if (config != null) {
                oos.writeInt(NetworkManager.RESPONSE);
                oos.writeObject(config);
                oos.flush();
            }
        }
        if (config != null) {
            getContext().getLogger().log(
                    Level.INFO,
                    "Player {0}@{1} joins room-{2}",
                    new Object[]{getUsername(), getClientAddress(), room_number});
        }
    }

    private void respondCreateRoom() throws IOException, ClassNotFoundException {
        String map_name = ois.readUTF();
        Map map = (Map) ois.readObject();
        int capacity = ois.readInt();
        int gold = ois.readInt();
        int population = ois.readInt();
        RoomConfig config = getContext().onPlayerCreateRoom(getName(), map_name, map, capacity, gold, population);
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(NetworkManager.RESPONSE);
            oos.writeObject(config);
            oos.flush();
        }
        if (config != null) {
            getContext().getLogger().log(
                    Level.INFO,
                    "Player {0}@{1} creates room-{2}",
                    new Object[]{getUsername(), getClientAddress(), room_number});
        }
    }

    private void respondUpdatePlayerType() throws IOException {
    }

    private void respondUpdateTeamAllocation() throws IOException {
    }

    private void respondUpdateAlliance() throws IOException {
    }

    private void respondStartGame() throws IOException {
        boolean approved = getContext().onGameStart(room_number, getName());
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(NetworkManager.RESPONSE);
            oos.writeBoolean(approved);
            oos.flush();
        }
        if (approved) {
            getContext().getLogger().log(
                    Level.INFO,
                    "Player {0}@{1} starts game",
                    new Object[]{getUsername(), getClientAddress()});
        }
    }

    public void respondGameEvent() throws IOException, ClassNotFoundException {
        GameEvent event = (GameEvent) ois.readObject();
        getContext().onSubmitGameEvent(getName(), event);
    }

    public void respondOperation(int opt) throws IOException {
        int x, y, index;
        switch (opt) {
            case GameHost.OPT_SELECT:
            case GameHost.OPT_ATTACK:
            case GameHost.OPT_SUMMON:
            case GameHost.OPT_MOVE_UNIT:
                x = ois.readInt();
                y = ois.readInt();
                processOperationRequest(opt, x, y);
                break;
            case GameHost.OPT_BUY:
                index = ois.readInt();
                x = ois.readInt();
                y = ois.readInt();
                processOperationRequest(opt, index, x, y);
                break;
            case GameHost.OPT_REVERSE_MOVE:
            case GameHost.OPT_END_TURN:
            case GameHost.OPT_STANDBY:
            case GameHost.OPT_OCCUPY:
            case GameHost.OPT_REPAIR:
                processOperationRequest(opt);
                break;
            default:
                //do nothing
        }
    }

    private void processOperationRequest(int request, Integer... params) throws IOException {
        Room room = getContext().getRoom(room_number);
        if (!getContext().isOpen(room)) {
            String host_service = room.getHostService();
            PlayerService host = getContext().getService(host_service);
            host.sendInteger(NetworkManager.REQUEST);
            host.sendInteger(Request.OPERATION);
            host.sendInteger(request);
            for (int i = 0; i < params.length; i++) {
                host.sendInteger(params[i]);
            }
        }
    }

    public void sendRequest(int request) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(NetworkManager.REQUEST);
            oos.writeInt(request);
            oos.flush();
        }
    }

    public void sendGameEvent(GameEvent event) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(NetworkManager.REQUEST);
            oos.writeInt(Request.GAME_EVENT);
            oos.writeObject(event);
            oos.flush();
        }
    }

    public void sendInteger(int n) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(n);
            oos.flush();
        }
    }

    @Override
    public void run() {
        try {
            oos.writeUTF(getName());
            oos.flush();

            username = ois.readUTF();
            getContext().getLogger().log(
                    Level.INFO,
                    "Player {0}@{1} connected",
                    new Object[]{getUsername(), getClientAddress()});
        } catch (IOException ex) {
            closeConnection();
        }

        while (client.isConnected()) {
            try {
                synchronized (INPUT_LOCK) {
                    int type = ois.readInt();
                    switch (type) {
                        case NetworkManager.REQUEST:
                            int request = ois.readInt();
                            processRequest(request);
                            break;
                        case NetworkManager.RESPONSE:
                            try {
                                INPUT_LOCK.wait();
                            } catch (InterruptedException e) {
                            }
                            break;
                        default:
                            //do nothing
                    }
                }
            } catch (IOException ex) {
                closeConnection();
                break;
            } catch (ClassNotFoundException ex) {
                getContext().getLogger().log(Level.SEVERE, ex.toString());
            }
        }
        getContext().onPlayerDisconnect(getName());
    }

    private void processRequest(int request) throws IOException, ClassNotFoundException {
        long room_number;
        switch (request) {
            case Request.LIST_ROOMS:
                respondOpenRooms();
                break;
            case Request.JOIN_ROOM:
                room_number = ois.readLong();
                respondJoinRoom(room_number);
                break;
            case Request.CREATE_ROOM:
                respondCreateRoom();
                break;
            case Request.UPDATE_PLAYER_TYPE:
                respondUpdatePlayerType();
                break;
            case Request.UPDATE_TEAM_ALLOCATION:
                respondUpdateTeamAllocation();
                break;
            case Request.UPDATE_ALLIANCE:
                respondUpdateAlliance();
                break;
            case Request.START_GAME:
                respondStartGame();
                break;
            case Request.GAME_EVENT:
                respondGameEvent();
                break;
            case Request.OPERATION:
                int opt = ois.readInt();
                respondOperation(opt);
                break;
            default:
                //do nothing
        }
    }

}
