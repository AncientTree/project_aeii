package com.toyknight.aeii.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.AsyncTask;
import com.toyknight.aeii.serializable.GameSave;
import com.toyknight.aeii.serializable.RoomConfig;
import com.toyknight.aeii.serializable.RoomSnapshot;
import com.toyknight.aeii.serializable.ServerConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Created by toyknight on 8/25/2015.
 */
public class NetworkManager {

    public static final String TAG = "Network";

    public static final int REQUEST = 0x1;
    public static final int RESPONSE = 0x2;

    private final Object INPUT_LOCK = new Object();
    private final Object OUTPUT_LOCK = new Object();

    private NetworkListener listener;

    private boolean running;

    private Socket server_socket;

    private String service_name;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public NetworkManager() {
        this.running = true;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        this.running = false;
    }

    public void setNetworkListener(NetworkListener listener) {
        this.listener = listener;
    }

    public NetworkListener getListener() {
        return listener;
    }

    public boolean connect(ServerConfig server, String username, String v_string) throws IOException {
        if (server_socket != null) {
            disconnect();
        }

        SocketHints hints = new SocketHints();
        server_socket = Gdx.net.newClientSocket(Net.Protocol.TCP, server.getAddress(), server.getPort(), hints);
        oos = new ObjectOutputStream(server_socket.getOutputStream());
        ois = new ObjectInputStream(server_socket.getInputStream());

        oos.writeUTF(username);
        oos.writeUTF(v_string);
        oos.flush();
        boolean approved = ois.readBoolean();
        if (approved) {
            service_name = ois.readUTF();
            new ReceivingThread().start();
        }
        return approved;
    }

    public void disconnect() {
        if (server_socket != null) {
            server_socket.dispose();
            server_socket = null;
            listener = null;
        }
    }

    public boolean isConnected() {
        return server_socket != null && server_socket.isConnected();
    }

    public String getServiceName() {
        return service_name;
    }

    public ArrayList<RoomSnapshot> requestOpenRoomList() throws IOException, ClassNotFoundException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.LIST_ROOMS);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            try {
                ArrayList list = (ArrayList) ois.readObject();
                INPUT_LOCK.notifyAll();
                return list;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            } catch (ClassNotFoundException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public RoomConfig requestCreateRoom(String map_name, Map map, int capacity, int gold, int population) throws IOException, ClassNotFoundException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.CREATE_ROOM);
            oos.writeUTF(map_name);
            oos.writeObject(map);
            oos.writeInt(capacity);
            oos.writeInt(gold);
            oos.writeInt(population);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            synchronized (INPUT_LOCK) {
                try {
                    RoomConfig config = (RoomConfig) ois.readObject();
                    INPUT_LOCK.notifyAll();
                    return config;
                } catch (IOException ex) {
                    INPUT_LOCK.notifyAll();
                    throw ex;
                }
            }
        }
    }

    public Object requestJoinRoom(long room_number) throws IOException, ClassNotFoundException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.JOIN_ROOM);
            oos.writeLong(room_number);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            try {
                Object response = ois.readObject();
                INPUT_LOCK.notifyAll();
                return response;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public void requestLeaveRoom() throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.LEAVE_ROOM);
            oos.flush();
        }
    }

    public void requestUpdateAllocation(String[] allocation, Integer[] types) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.UPDATE_ALLOCATION);
            for (int team = 0; team < 4; team++) {
                oos.writeUTF(allocation[team]);
                oos.writeInt(types[team]);
            }
            oos.flush();
        }
    }

    public void requestUpdateAlliance(Integer[] alliance) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.UPDATE_ALLIANCE);
            for (int team = 0; team < 4; team++) {
                oos.writeInt(alliance[team]);
            }
            oos.flush();
        }
    }

    public boolean requestStartGame() throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.START_GAME);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            try {
                boolean approved = ois.readBoolean();
                INPUT_LOCK.notifyAll();
                return approved;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }
    public boolean requestStartGame(GameSave game_save) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.RESUME_GAME);
            oos.writeObject(game_save);
            oos.flush();
        }
        synchronized (INPUT_LOCK) {
            try {
                boolean approved = ois.readBoolean();
                INPUT_LOCK.notifyAll();
                return approved;
            } catch (IOException ex) {
                INPUT_LOCK.notifyAll();
                throw ex;
            }
        }
    }

    public void sendGameEvent(GameEvent event) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.GAME_EVENT);
            oos.writeObject(event);
            oos.flush();
        }
        Gdx.app.log(TAG, "Send " + event.toString());
    }

    public void requestSubmitMessage(String message) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.MESSAGE);
            oos.writeUTF(message);
            oos.flush();
        }
    }

    private class ReceivingThread extends Thread {

        @Override
        public void run() {
            while (server_socket.isConnected() && isRunning()) {
                try {
                    synchronized (INPUT_LOCK) {
                        int type = ois.readInt();
                        switch (type) {
                            case REQUEST:
                                int request = ois.readInt();
                                processRequest(request);
                                break;
                            case RESPONSE:
                                //It's a server response. Free the input stream for task thread to read the response
                                try {
                                    INPUT_LOCK.wait();
                                } catch (InterruptedException ignored) {
                                }
                                break;
                            default:
                                //do nothing
                        }
                    }
                } catch (IOException e) {
                    Gdx.app.log(TAG, e.getMessage());
                    break;
                } catch (ClassNotFoundException e) {
                    Gdx.app.log(TAG, e.toString());
                }
            }
            if (server_socket != null) {
                disconnect();
                if (listener != null) {
                    listener.onDisconnect();
                }
            }
            Gdx.app.log(TAG, "Disconnected from server");
        }

        private void processRequest(int request) throws IOException, ClassNotFoundException {
            String service_name, username, message;
            switch (request) {
                case Request.START_GAME:
                    synchronized (AEIIApplication.RENDER_LOCK) {
                        getListener().onGameStart(null);
                    }
                    break;
                case Request.RESUME_GAME:
                    GameSave game_save = (GameSave) ois.readObject();
                    synchronized (AEIIApplication.RENDER_LOCK) {
                        getListener().onGameStart(game_save);
                    }
                case Request.GAME_EVENT:
                    GameEvent event = (GameEvent) ois.readObject();
                    Gdx.app.log(TAG, "Receive " + event.toString());
                    synchronized (AEIIApplication.RENDER_LOCK) {
                        getListener().onReceiveGameEvent(event);
                    }
                    break;
                case Request.MESSAGE:
                    username = ois.readUTF();
                    message = ois.readUTF();
                    synchronized (AEIIApplication.RENDER_LOCK) {
                        if (getListener() != null) {
                            getListener().onReceiveMessage(username, message);
                        }
                    }
                    break;
                case Request.PLAYER_JOINING:
                    service_name = ois.readUTF();
                    username = ois.readUTF();
                    synchronized (AEIIApplication.RENDER_LOCK) {
                        if (getListener() != null) {
                            getListener().onPlayerJoin(service_name, username);
                        }
                    }
                    break;
                case Request.PLAYER_LEAVING:
                    service_name = ois.readUTF();
                    username = ois.readUTF();
                    synchronized (AEIIApplication.RENDER_LOCK) {
                        if (getListener() != null) {
                            getListener().onPlayerLeave(service_name, username);
                        }
                    }
                    break;
                case Request.UPDATE_ALLOCATION:
                    String[] allocation = new String[4];
                    for (int team = 0; team < 4; team++) {
                        allocation[team] = ois.readUTF();
                    }
                    Integer[] types = new Integer[4];
                    for (int team = 0; team < 4; team++) {
                        types[team] = ois.readInt();
                    }
                    synchronized (AEIIApplication.RENDER_LOCK) {
                        if (getListener() != null) {
                            getListener().onAllocationUpdate(allocation, types);
                        }
                    }
                    break;
                case Request.UPDATE_ALLIANCE:
                    Integer[] alliance = new Integer[4];
                    for (int team = 0; team < 4; team++) {
                        alliance[team] = ois.readInt();
                    }
                    synchronized (AEIIApplication.RENDER_LOCK) {
                        if (getListener() != null) {
                            getListener().onAllianceUpdate(alliance);
                        }
                    }
                    break;
                case Request.GET_GAME:
                    GameCore game = GameHost.getGame();
                    synchronized (OUTPUT_LOCK) {
                        oos.writeInt(NetworkManager.RESPONSE);
                        oos.writeObject(game);
                        oos.flush();
                    }
                    break;
                default:
                    //do nothing
            }
        }

    }

}
