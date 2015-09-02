package com.toyknight.aeii.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.net.task.NetworkTask;
import com.toyknight.aeii.server.entity.RoomConfig;
import com.toyknight.aeii.server.entity.RoomSnapshot;
import com.toyknight.aeii.server.entity.Server;
import com.toyknight.aeii.utils.Language;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
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

    private final ExecutorService executor;

    private NetworkListener listener;

    private boolean running;

    private Socket server_socket;

    private String service_name;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public NetworkManager() {
        this.executor = Executors.newSingleThreadExecutor();
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

    public void connect(Server server, String username) throws IOException {
        if (server_socket != null) {
            disconnect();
        }

        SocketHints hints = new SocketHints();
        server_socket = Gdx.net.newClientSocket(Net.Protocol.TCP, server.getAddress(), server.getPort(), hints);
        oos = new ObjectOutputStream(server_socket.getOutputStream());
        ois = new ObjectInputStream(server_socket.getInputStream());

        service_name = ois.readUTF();
        oos.writeUTF(username);
        oos.flush();

        new ReceivingThread().start();
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

    public void postTask(NetworkTask task) {
        executor.submit(task);
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

    public RoomConfig requestJoinRoom(long room_number) throws IOException, ClassNotFoundException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.JOIN_ROOM);
            oos.writeLong(room_number);
            oos.flush();
        }
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

    public void requestLeaveRoom() throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.LEAVE_ROOM);
            oos.flush();
        }
    }

    public boolean requestUpdatePlayerType() throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.UPDATE_PLAYER_TYPE);

            oos.flush();
        }
        return false;
    }

    public boolean requestUpdateTeamAllocation() throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.UPDATE_TEAM_ALLOCATION);

            oos.flush();
        }
        return false;
    }

    public boolean requestUpdateAlliance() throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.UPDATE_ALLIANCE);

            oos.flush();
        }
        return false;
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

    public void sendGameEvent(GameEvent event) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(REQUEST);
            oos.writeInt(Request.GAME_EVENT);
            oos.writeObject(event);
            oos.flush();
        }
        Gdx.app.log(TAG, "Send " + event.toString());
    }

    public void sendInteger(int n) throws IOException {
        synchronized (OUTPUT_LOCK) {
            oos.writeInt(n);
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
            String service_name, username;
            switch (request) {
                case Request.START_GAME:
                    getListener().onGameStart();
                    break;
                case Request.GAME_EVENT:
                    GameEvent event = (GameEvent) ois.readObject();
                    Gdx.app.log(TAG, "Receive " + event.toString());
                    getListener().onReceiveGameEvent(event);
                    break;
                case Request.OPERATION:
                    int opt = ois.readInt();
                    processOperation(opt);
                    break;
                case Request.PLAYER_JOINING:
                    service_name = ois.readUTF();
                    username = ois.readUTF();
                    if (getListener() != null) {
                        getListener().onPlayerJoin(service_name, username);
                    }
                    break;
                case Request.PLAYER_LEAVING:
                    service_name = ois.readUTF();
                    username = ois.readUTF();
                    if (getListener() != null) {
                        getListener().onPlayerLeave(service_name, username);
                    }
                    break;
                default:
                    //do nothing
            }
        }

        private void processOperation(int opt) throws IOException {
            int index, x, y;
            switch (opt) {
                case GameHost.OPT_ATTACK:
                    x = ois.readInt();
                    y = ois.readInt();
                    GameHost.doAttack(x, y);
                    break;
                case GameHost.OPT_BUY:
                    index = ois.readInt();
                    x = ois.readInt();
                    y = ois.readInt();
                    GameHost.doBuyUnit(index, x, y);
                    break;
                case GameHost.OPT_END_TURN:
                    GameHost.doEndTurn();
                    break;
                case GameHost.OPT_MOVE_UNIT:
                    x = ois.readInt();
                    y = ois.readInt();
                    GameHost.doMoveUnit(x, y);
                    break;
                case GameHost.OPT_OCCUPY:
                    GameHost.doOccupy();
                    break;
                case GameHost.OPT_REPAIR:
                    GameHost.doRepair();
                    break;
                case GameHost.OPT_REVERSE_MOVE:
                    GameHost.doReverseMove();
                    break;
                case GameHost.OPT_SELECT:
                    x = ois.readInt();
                    y = ois.readInt();
                    GameHost.doSelect(x, y);
                    break;
                case GameHost.OPT_STANDBY:
                    GameHost.doStandbyUnit();
                    break;
                case GameHost.OPT_SUMMON:
                    x = ois.readInt();
                    y = ois.readInt();
                    GameHost.doSummon(x, y);
                    break;
                default:
                    //do nothing;
            }
        }

    }

}
