package com.toyknight.aeii.net.server;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.toyknight.aeii.AEIIException;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.net.Request;
import com.toyknight.aeii.net.Response;
import com.toyknight.aeii.serializable.RoomSnapshot;
import com.toyknight.aeii.server.entity.Room;
import com.toyknight.aeii.utils.ClassRegister;
import com.toyknight.aeii.utils.Encryptor;
import com.toyknight.aeii.utils.TileFactory;
import com.toyknight.aeii.utils.UnitFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author toyknight 10/27/2015.
 */
public class GameServer {

    private static final Logger logger = Logger.getLogger("com.toyknight.aeii.net.server");

    private String V_STRING;

    private Server server;

    private ExecutorService executor;

    private final Object PLAYER_LOCK = new Object();
    private ObjectMap<Integer, PlayerService> players;

    private final Object ROOM_LOCK = new Object();
    private long current_room_number;
    private ObjectMap<Long, Room> rooms;

    public Logger getLogger() {
        return logger;
    }

    public PlayerService getPlayer(int id) {
        synchronized (PLAYER_LOCK) {
            return players.get(id);
        }
    }

    public void removePlayer(int id) {
        synchronized (PLAYER_LOCK) {
            players.remove(id);
        }
    }

    public boolean isRoomOpen(long room_number) {
        synchronized (ROOM_LOCK) {
            Room room = rooms.get(room_number);
            return room != null && room.isOpen();
        }
    }

    public Room getRoom(long room_number) {
        synchronized (ROOM_LOCK) {
            return rooms.get(room_number);
        }
    }

    public void addRoom(Room room) {
        synchronized (ROOM_LOCK) {
            rooms.put(room.getRoomNumber(), room);
        }
    }

    public void removeRoom(long room_number) {
        synchronized (ROOM_LOCK) {
            rooms.remove(room_number);
        }
        getLogger().log(Level.INFO, "Dispose room-{0}", room_number);
    }

    public Array<RoomSnapshot> getRoomsSnapshots() {
        synchronized (ROOM_LOCK) {
            Array<RoomSnapshot> snapshots = new Array<RoomSnapshot>();
            ObjectMap.Values<Room> room_list = rooms.values();
            while (room_list.hasNext()) {
                snapshots.add(room_list.next().createSnapshot());
            }
            return snapshots;
        }
    }

    public void onPlayerConnect(Connection connection) {
        int id = connection.getID();
        PlayerService player = new PlayerService(connection);
        players.put(id, player);
    }

    public void onPlayerDisconnect(Connection connection) {
        PlayerService player = getPlayer(connection.getID());
        String username = player.getUsername();
        String address = player.getAddress();
        //onPlayerLeaveRoom(service_name, username);
        removePlayer(connection.getID());
        getLogger().log(
                Level.INFO,
                "Player {0}@{1} disconnected",
                new Object[]{username, address});
    }

    public void onReceive(Connection connection, Object object) {
        PlayerService player = getPlayer(connection.getID());
        if (object instanceof Request) {
            onReceiveRequest(player, (Request) object);
        }
    }

    public void onReceiveRequest(PlayerService player, Request request) {
        switch (request.getType()) {
            case Request.AUTHENTICATION:
                doAuthentication(player, request);
                break;
            case Request.LIST_ROOMS:
                doRespondRoomList(player, request);
                break;
            case Request.CREATE_ROOM:
                doRespondCreateRoom(player, request);
                break;
            default:
                //do nothing
        }
    }

    public void doAuthentication(PlayerService player, Request request) {
        RequestProcessingTask task = new RequestProcessingTask(player, request) {
            @Override
            public void processRequest() {
                Response response = new Response(getRequest().getID());

                String username = (String) getRequest().getParameter(0);
                String v_string = (String) getRequest().getParameter(1);

                if (V_STRING.equals(v_string)) {
                    getPlayer().setAuthenticated(true);
                    getPlayer().setUsername(username);
                    response.setParameters(true);
                } else {
                    response.setParameters(false);
                }
                getPlayer().getConnection().sendTCP(response);
            }
        };
        executor.submit(task);
    }

    public void doRespondRoomList(PlayerService player, Request request) {
        RequestProcessingTask task = new RequestProcessingTask(player, request) {
            @Override
            public void processRequest() {
                Response response = new Response(getRequest().getID());
                if (getPlayer().isAuthenticated()) {
                    Array<RoomSnapshot> snapshots = getRoomsSnapshots();
                    response.setParameters(snapshots);
                }
                getPlayer().getConnection().sendTCP(response);
            }
        };
        executor.submit(task);
    }

    public void doRespondCreateRoom(PlayerService player, Request request) {
        RequestProcessingTask task = new RequestProcessingTask(player, request) {
            @Override
            public void processRequest() {
                Response response = new Response(getRequest().getID());
                if (getPlayer().isAuthenticated()) {
                    Room room = new Room(current_room_number++, getPlayer().getUsername() + "'s game");
//                    room.setMapName((String) request.getParameter(0));
//                    room.setMap((Map) request.getParameter(1));
//                    room.setCapacity(capacity);
//                    room.setInitialGold(gold);
//                    room.setMaxPopulation(population);
//                    room.setHostService(service_name);
                    addRoom(room);
                    getPlayer().getConnection().sendTCP(response);
                }
            }
        };
        executor.submit(task);
    }

    public String getVerificationString() {
        String V_STRING =
                TileFactory.getVerificationString() + UnitFactory.getVerificationString() + GameContext.VERSION;
        return new Encryptor().encryptString(V_STRING);
    }

    private void create() throws AEIIException {
        executor = Executors.newFixedThreadPool(64);
        server = new Server();
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                onPlayerConnect(connection);
            }

            @Override
            public void disconnected(Connection connection) {
                onPlayerDisconnect(connection);
            }

            @Override
            public void received(Connection connection, Object object) {
                onReceive(connection, object);
            }
        });
        new ClassRegister().register(server.getKryo());
        UnitFactory.loadUnitData();
        TileFactory.loadTileData();
        V_STRING = getVerificationString();
        players = new ObjectMap<Integer, PlayerService>();
        rooms = new ObjectMap<Long, Room>();
        current_room_number = 0;
    }

    public void start() {
        try {
            create();
            server.start();
            server.bind(5438);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } catch (AEIIException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    private abstract class RequestProcessingTask implements Runnable {

        private final PlayerService player;
        private final Request request;

        public RequestProcessingTask(PlayerService player, Request request) {
            this.player = player;
            this.request = request;
        }

        protected PlayerService getPlayer() {
            return player;
        }

        protected Request getRequest() {
            return request;
        }

        @Override
        public final void run() {
            if (getRequest().getType() == Request.AUTHENTICATION || getPlayer().isAuthenticated()) {
                processRequest();
            }
        }

        abstract public void processRequest();

    }


}
