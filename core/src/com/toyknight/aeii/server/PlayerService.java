package com.toyknight.aeii.server;

import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.net.NetworkManager;
import com.toyknight.aeii.net.Request;
import com.toyknight.aeii.server.entity.RoomConfig;
import com.toyknight.aeii.server.entity.RoomSnapshot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * @author toyknight
 */
public class PlayerService extends Thread {

    private final Object INPUT_LOCK = new Object();
    private final Object OUTPUT_LOCK = new Object();

    private final ExecutorService executor;

    private final AEIIServer context;
    private final Socket client;

    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;

    private String username;
    private long room_number;

    public PlayerService(AEIIServer context, Socket client, String name) throws IOException {
        super(context.getServiceGroup(), name);

        this.executor = Executors.newSingleThreadExecutor();
        this.context = context;
        this.client = client;

        this.oos = new ObjectOutputStream(client.getOutputStream());
        this.ois = new ObjectInputStream(client.getInputStream());
        this.oos.flush();
    }

    private AEIIServer getContext() {
        return context;
    }

    public void create() {
        this.username = "undefined";
        this.room_number = -1;
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

    private void closeConnection() {
        try {
            client.close();
        } catch (IOException ex) {
            getContext().getLogger().log(Level.WARNING, ex.toString());
        }
    }

    public void notifyPlayerJoining(String service_name, String username) {
        NotificationTask task = new NotificationTask(Request.PLAYER_JOINING, service_name, username);
        executor.submit(task);
    }

    public void notifyPlayerLeaving(String service_name, String username) {
        NotificationTask task = new NotificationTask(Request.PLAYER_LEAVING, service_name, username);
        executor.submit(task);
    }

    public void notifyAllocation(String[] team_allocation, Integer[] player_type) {
        Object[] params = new Object[team_allocation.length + player_type.length];
        System.arraycopy(team_allocation, 0, params, 0, team_allocation.length);
        System.arraycopy(player_type, 0, params, team_allocation.length, player_type.length);
        NotificationTask task = new NotificationTask(Request.UPDATE_ALLOCATION, params);
        executor.submit(task);
    }

    public void notifyAlliance(Integer[] alliance) {
        NotificationTask task = new NotificationTask(Request.UPDATE_ALLIANCE, alliance);
        executor.submit(task);
    }

    public void notifyGameStart() {
        NotificationTask task = new NotificationTask(Request.START_GAME);
        executor.submit(task);
    }

    public void notifyGameEvent(GameEvent event) {
        NotificationTask task = new NotificationTask(Request.GAME_EVENT, event);
        executor.submit(task);
    }

    public void notifyOperation(Integer... params) {
        NotificationTask task = new NotificationTask(Request.OPERATION, params);
        executor.submit(task);
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
            oos.writeInt(NetworkManager.RESPONSE);
            oos.writeObject(config);
            oos.flush();
        }
        if (config != null) {
            getContext().getLogger().log(
                    Level.INFO,
                    "Player {0}@{1} joins room-{2}",
                    new Object[]{getUsername(), getClientAddress(), room_number});
        }
    }

    private void respondLeaveRoom() {
        if (getRoomNumber() >= 0) {
            getContext().onPlayerLeaveRoom(getName());
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

    private void respondUpdateAllocation() throws IOException {
        String[] allocation = new String[4];
        Integer[] types = new Integer[4];
        for (int team = 0; team < 4; team++) {
            allocation[team] = ois.readUTF();
            types[team] = ois.readInt();
        }
        getContext().onUpdateAllocation(getName(), allocation, types);
    }

    private void respondUpdateAlliance() throws IOException {
        Integer[] alliance = new Integer[4];
        for (int team = 0; team < 4; team++) {
            alliance[team] = ois.readInt();
        }
        getContext().onUpdateAlliance(getName(), alliance);
    }

    private void respondStartGame() throws IOException {
        boolean approved = getContext().onStartGame(room_number, getName());
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
                getContext().getHostService(room_number).notifyOperation(opt, x, y);
                //processOperationRequest(opt, x, y);
                break;
            case GameHost.OPT_BUY:
                index = ois.readInt();
                x = ois.readInt();
                y = ois.readInt();
                getContext().getHostService(room_number).notifyOperation(opt, index, x, y);
                //processOperationRequest(opt, index, x, y);
                break;
            case GameHost.OPT_REVERSE_MOVE:
            case GameHost.OPT_END_TURN:
            case GameHost.OPT_STANDBY:
            case GameHost.OPT_OCCUPY:
            case GameHost.OPT_REPAIR:
                getContext().getHostService(room_number).notifyOperation(opt);
                //processOperationRequest(opt);
                break;
            default:
                //do nothing
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
            case Request.LEAVE_ROOM:
                respondLeaveRoom();
                break;
            case Request.CREATE_ROOM:
                respondCreateRoom();
                break;
            case Request.UPDATE_ALLOCATION:
                respondUpdateAllocation();
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

    private class NotificationTask implements Runnable {

        private final int request;
        private final Object[] params;

        public NotificationTask(int request, Object... params) {
            this.request = request;
            this.params = params;
        }

        @Override
        public void run() {
            try {
                synchronized (OUTPUT_LOCK) {
                    oos.writeInt(NetworkManager.REQUEST);
                    oos.writeInt(request);
                    for (Object obj : params) {
                        if (obj instanceof Integer) {
                            oos.writeInt((Integer) obj);
                            continue;
                        }
                        if (obj instanceof Long) {
                            oos.writeLong((Long) obj);
                            continue;
                        }
                        if (obj instanceof String) {
                            oos.writeUTF((String) obj);
                            continue;
                        }
                        if (obj instanceof Serializable) {
                            oos.writeObject(obj);
                            continue;
                        }
                    }
                    oos.flush();
                }
            } catch (IOException ex) {
                getContext().getLogger().log(Level.SEVERE, ex.toString());
            }
        }

    }

}
