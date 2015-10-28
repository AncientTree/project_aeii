package com.toyknight.aeii.server;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.net.NetworkManager;
import com.toyknight.aeii.net.Request;
import com.toyknight.aeii.serializable.GameSave;
import com.toyknight.aeii.serializable.RoomConfiguration;
import com.toyknight.aeii.serializable.RoomCreationSetup;
import com.toyknight.aeii.serializable.RoomSnapshot;

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

    public void closeConnection() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
        if (client.isConnected()) {
            try {
                client.close();
            } catch (IOException ex) {
                getContext().getLogger().log(Level.WARNING, ex.toString());
            }
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

    public void notifyGameResume(GameSave game_save) {
        NotificationTask task = new NotificationTask(Request.RESUME_GAME, game_save);
        executor.submit(task);
    }

    public void notifyGameEvent(GameEvent event) {
        NotificationTask task = new NotificationTask(Request.GAME_EVENT, event);
        executor.submit(task);
    }

    public void notifyMessage(String username, String message) {
        NotificationTask task = new NotificationTask(Request.MESSAGE, username, message);
        executor.submit(task);
    }

    public GameCore requestGame() throws IOException, ClassNotFoundException {
        synchronized (OUTPUT_LOCK) {
            sendInteger(NetworkManager.REQUEST);
            sendInteger(Request.GET_GAME);
        }
        synchronized (INPUT_LOCK) {
            GameCore game = (GameCore) ois.readObject();
            INPUT_LOCK.notifyAll();
            return game;
        }
    }

    private void respondOpenRooms() throws IOException {
        ArrayList<RoomSnapshot> snapshot = getContext().getRoomSnapshot();
        synchronized (OUTPUT_LOCK) {
            sendInteger(NetworkManager.RESPONSE);
            sendObject(snapshot);
        }
        getContext().getLogger().log(
                Level.INFO,
                "Player {0}@{1} fetches room list",
                new Object[]{getUsername(), getClientAddress()});
    }

    private void respondJoinRoom(long room_number) throws IOException {
        if (getContext().isRoomOpen(room_number)) {
            RoomConfiguration config = getContext().onPlayerJoinRoom(getName(), room_number);
            synchronized (OUTPUT_LOCK) {
                sendInteger(NetworkManager.RESPONSE);
                sendObject(config);
            }
        } else {
            synchronized (OUTPUT_LOCK) {
                GameCore game = getContext().onPlayerJoinStartedGame(getName(), room_number);
                sendInteger(NetworkManager.RESPONSE);
                sendObject(game);
            }
        }
        getContext().getLogger().log(
                Level.INFO,
                "Player {0}@{1} tries to join room-{2}",
                new Object[]{getUsername(), getClientAddress(), room_number});
    }

    private void respondLeaveRoom() {
        if (getRoomNumber() >= 0) {
            getContext().onPlayerLeaveRoom(getName(), getUsername());
        }
    }

    private void respondCreateRoom(String map_name, Map map, int capacity, int gold, int population) throws IOException {
        RoomConfiguration config = getContext().onPlayerCreateRoom(getName(), map_name, map, capacity, gold, population);
        synchronized (OUTPUT_LOCK) {
            sendInteger(NetworkManager.RESPONSE);
            sendObject(config);
        }
        if (config != null) {
            getContext().getLogger().log(
                    Level.INFO,
                    "Player {0}@{1} creates room-{2}",
                    new Object[]{getUsername(), getClientAddress(), room_number});
        }
    }

    private void respondStartGame() throws IOException {
        boolean approved = getContext().onStartGame(room_number, getName());
        synchronized (OUTPUT_LOCK) {
            sendInteger(NetworkManager.RESPONSE);
            sendBoolean(approved);
        }
        if (approved) {
            getContext().getLogger().log(
                    Level.INFO,
                    "Player {0}@{1} starts game",
                    new Object[]{getUsername(), getClientAddress()});
        }
    }

    private void respondResumeGame(GameSave game_save) throws IOException {
        boolean approved = getContext().onResumeGame(room_number, getName(), game_save);
        synchronized (OUTPUT_LOCK) {
            sendInteger(NetworkManager.RESPONSE);
            sendBoolean(approved);
        }
        if (approved) {
            getContext().getLogger().log(
                    Level.INFO,
                    "Player {0}@{1} starts game",
                    new Object[]{getUsername(), getClientAddress()});
        }
    }

    public void sendInteger(int n) {
        boolean sent = false;
        while (!sent && client.isConnected()) {
            try {
                oos.writeInt(n);
                oos.flush();
                sent = true;
            } catch (IOException ex) {
                getContext().getLogger().log(Level.SEVERE, ex.toString());
            }
        }
    }

    public void sendLong(long n) {
        boolean sent = false;
        while (!sent && client.isConnected()) {
            try {
                oos.writeLong(n);
                oos.flush();
                sent = true;
            } catch (IOException ex) {
                getContext().getLogger().log(Level.SEVERE, ex.toString());
            }
        }
    }

    public void sendBoolean(boolean b) {
        boolean sent = false;
        while (!sent && client.isConnected()) {
            try {
                oos.writeBoolean(b);
                oos.flush();
                sent = true;
            } catch (IOException ex) {
                getContext().getLogger().log(Level.SEVERE, ex.toString());
            }
        }
    }

    public void sendObject(Object obj) {
        boolean sent = false;
        while (!sent && client.isConnected()) {
            try {
                oos.writeObject(obj);
                oos.flush();
                sent = true;
            } catch (IOException ex) {
                getContext().getLogger().log(Level.SEVERE, ex.toString());
            }
        }
    }

    public void sendString(String str) {
        boolean sent = false;
        while (!sent && client.isConnected()) {
            try {
                oos.writeUTF(str);
                oos.flush();
                sent = true;
            } catch (IOException ex) {
                getContext().getLogger().log(Level.SEVERE, ex.toString());
            }
        }
    }

    @Override
    public void run() {
        try {
            username = ois.readUTF();
            String v_string = ois.readUTF();
            if (getContext().getVerificationString().equals(v_string)) {
                sendBoolean(true);
                sendString(getName());

                getContext().getLogger().log(
                        Level.INFO,
                        "Player {0}@{1} connected",
                        new Object[]{getUsername(), getClientAddress()});
            } else {
                sendBoolean(false);
                getContext().getLogger().log(
                        Level.INFO,
                        "Player {0}@{1} verification failed",
                        new Object[]{getUsername(), getClientAddress()});
            }
        } catch (IOException ex) {
            closeConnection();
        }

        while (client.isConnected() && getContext().isRunning()) {
            try {
                synchronized (INPUT_LOCK) {
                    int type = ois.readInt();
                    switch (type) {
                        case NetworkManager.REQUEST:
                            int request = ois.readInt();
                            switch (request) {
                                case Request.LIST_ROOMS:
                                    respondOpenRooms();
                                    break;
                                case Request.JOIN_ROOM:
                                    long room_number = ois.readLong();
                                    respondJoinRoom(room_number);
                                    break;
                                case Request.LEAVE_ROOM:
                                    respondLeaveRoom();
                                    break;
                                case Request.CREATE_ROOM:
                                    RoomCreationSetup setup = (RoomCreationSetup) ois.readObject();
                                    String map_name = setup.map_name;
                                    Map map = setup.map;
                                    int capacity = setup.capacity;
                                    int gold = setup.initial_gold;
                                    int population = setup.population;
                                    respondCreateRoom(map_name, map, capacity, gold, population);
                                    break;
                                case Request.UPDATE_ALLOCATION:
                                    String[] allocation = new String[4];
                                    Integer[] types = new Integer[4];
                                    for (int team = 0; team < 4; team++) {
                                        allocation[team] = ois.readUTF();
                                        types[team] = ois.readInt();
                                    }
                                    getContext().onUpdateAllocation(getName(), allocation, types);
                                    break;
                                case Request.UPDATE_ALLIANCE:
                                    Integer[] alliance = new Integer[4];
                                    for (int team = 0; team < 4; team++) {
                                        alliance[team] = ois.readInt();
                                    }
                                    getContext().onUpdateAlliance(getName(), alliance);
                                    break;
                                case Request.START_GAME:
                                    respondStartGame();
                                    break;
                                case Request.RESUME_GAME:
                                    GameSave game_save = (GameSave) ois.readObject();
                                    respondResumeGame(game_save);
                                    break;
                                case Request.GAME_EVENT:
                                    GameEvent event = (GameEvent) ois.readObject();
                                    getContext().onSubmitGameEvent(getName(), event);
                                    break;
                                case Request.MESSAGE:
                                    String message = ois.readUTF();
                                    getContext().onSubmitMessage(getName(), message);
                                    break;
                                case Request.SHUTDOWN:
                                    String password = ois.readUTF();
                                    getContext().onShutdownRequested(password);
                                    break;
                                default:
                                    //do nothing
                            }
                            break;
                        case NetworkManager.RESPONSE:
                            try {
                                INPUT_LOCK.wait();
                            } catch (InterruptedException ignore) {
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
                break;
            }
        }
        getContext().onPlayerDisconnect(getName());
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
            synchronized (OUTPUT_LOCK) {
                sendInteger(NetworkManager.REQUEST);
                sendInteger(request);
                for (Object obj : params) {
                    if (obj instanceof Integer) {
                        sendInteger((Integer) obj);
                        continue;
                    }
                    if (obj instanceof Long) {
                        sendLong((Long) obj);
                        continue;
                    }
                    if (obj instanceof String) {
                        sendString((String) obj);
                        continue;
                    }
                    if (obj instanceof Serializable) {
                        sendObject(obj);
                    }
                }
            }
        }
    }

}
