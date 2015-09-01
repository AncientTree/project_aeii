package com.toyknight.aeii.server;

import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.net.Request;
import com.toyknight.aeii.server.entity.Room;
import com.toyknight.aeii.server.entity.RoomConfig;
import com.toyknight.aeii.server.entity.RoomSnapshot;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author toyknight
 */
public class AEIIServer {

    private static final Logger logger = Logger.getLogger("com.toyknight.aeii.server");

    private final Object SERVICE_LOCK = new Object();
    private final Object ROOM_LOCK = new Object();

    private final String TAG_SERVICE = "service-";

    private boolean running;
    private long current_service_number;

    private ServerSocket server;
    private ThreadGroup service_group;
    private HashMap<String, PlayerService> services;

    private long current_room_number;
    private HashMap<Long, Room> rooms;

    private void create() throws IOException {
        running = true;
        current_service_number = 0;

        service_group = new ThreadGroup("service-group");
        server = new ServerSocket(5438);

        services = new HashMap();

        current_room_number = 0;
        rooms = new HashMap();

        //create test room
        try {
            Room test_room = new Room(current_room_number++, "Server's test game");
            File map_file = new File("Mourningstar.saem");
            FileInputStream fis = new FileInputStream(map_file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Map map = (Map) ois.readObject();
            test_room.setInitialGold(2000);
            test_room.setMap(map);
            test_room.setMapName(map_file.getName());
            rooms.put(test_room.getRoomNumber(), test_room);
        } catch (Exception ex) {
        }
    }

    public ThreadGroup getServiceGroup() {
        return service_group;
    }

    public Logger getLogger() {
        return logger;
    }

    public PlayerService getService(String service_name) {
        return services.get(service_name);
    }

    public void removeService(String service_name) {
        synchronized (SERVICE_LOCK) {
            services.remove(service_name);
        }
    }

    public Room getRoom(long room_number) {
        return rooms.get(room_number);
    }

    public RoomConfig createRoomConfig(Room room) {
        RoomConfig config = new RoomConfig();
        config.room_number = room.getRoomNumber();
        config.host = room.getHostService();
        config.map = room.getMap();
        config.team_allocation = room.getTeamAllocation();
        config.player_type = room.getPlayerType();
        config.alliance_state = room.getAllianceState();
        config.initial_gold = room.getInitialGold();
        config.max_population = room.getMaxPopulation();
        return config;
    }

    public boolean isOpen(Room room) {
        return room != null && room.isOpen();
    }

    public void removeRoom(long room_number) {
        synchronized (ROOM_LOCK) {
            rooms.remove(room_number);
        }
    }

    public String getUsername(String service_name) {
        return getService(service_name).getUsername();
    }

    public String getClientAddress(String service_name) {
        return getService(service_name).getClientAddress();
    }

    public boolean isSystemRoom(long room_number) {
        return room_number == 0;
    }

    public ArrayList<RoomSnapshot> getOpenRoomSnapshot() {
        ArrayList<RoomSnapshot> snapshot = new ArrayList();
        synchronized (ROOM_LOCK) {
            for (Room room : rooms.values()) {
                if (room.isOpen()) {
                    snapshot.add(room.createSnapshot());
                }
            }
        }
        return snapshot;
    }

    public RoomConfig onPlayerJoinRoom(String service_name, long room_number) {
        Room room = getRoom(room_number);
        if (isOpen(room) && room.getRemaining() > 0) {
            room.addPlayer(service_name);
            getService(service_name).setRoomNumber(room_number);
            return createRoomConfig(room);
        } else {
            return null;
        }
    }

    public RoomConfig onPlayerCreateRoom(String service_name, String map_name, Map map, int capacity, int gold, int population) {
        PlayerService player = getService(service_name);
        Room room = new Room(current_room_number++, player.getUsername() + "'s game");
        room.setMapName(map_name);
        room.setMap(map);
        room.setCapacity(capacity);
        room.setInitialGold(gold);
        room.setMaxPopulation(population);
        room.addPlayer(service_name);
        player.setRoomNumber(room.getRoomNumber());
        rooms.put(room.getRoomNumber(), room);
        return createRoomConfig(room);
    }

    public boolean onGameStart(long room_number, String requester) {
        Room room = getRoom(room_number);
        if (room == null || !room.getHostService().equals(requester) || !room.isReady()) {
            return false;
        } else {
            try {
                for (String service_name : room.getPlayers()) {
                    if (!service_name.equals(requester)) {
                        getService(service_name).sendRequest(Request.START_GAME);
                    }
                }
                room.setGameStarted(true);
                return true;
            } catch (IOException ex) {
                return false;
            }
        }
    }

    public boolean onSubmitGameEvent(String service_name, GameEvent event) throws IOException {
        PlayerService service = getService(service_name);
        Room room = getRoom(service.getRoomNumber());
        if (room == null || room.isOpen()) {
            return false;
        } else {
            Set<String> players = room.getPlayers();
            for (String player_service : players) {
                if (!player_service.equals(service_name)) {
                    getService(player_service).sendGameEvent(event);
                }
            }
            return true;
        }
    }

    public void onPlayerLeaveRoom(String service_name) {
        PlayerService service = getService(service_name);
        long room_number = service.getRoomNumber();
        if (room_number >= 0) {
            Room room = getRoom(room_number);
            room.removePlayer(service_name);
            if (room.getCapacity() == room.getRemaining()) {
                if (isSystemRoom(room.getRoomNumber())) {
                    room.reset();
                } else {
                    removeRoom(room_number);
                }
            } else {
                for (String player : room.getPlayers()) {
                    PlayerService player_service = getService(player);
                    if (player_service != null && !player.equals(service_name)) {
                        try {
                            player_service.notifyPlayerLeaving(service_name, player_service.getUsername());
                        } catch (IOException ex) {
                            getLogger().log(Level.SEVERE, ex.toString());
                        }
                    }
                }
            }
            service.setRoomNumber(-1);
            getLogger().log(
                    Level.INFO,
                    "Player {0}@{1} leaves room-{2}",
                    new Object[]{getUsername(service_name), getClientAddress(service_name), room_number});
        }
    }

    public void onPlayerDisconnect(String service_name) {
        onPlayerLeaveRoom(service_name);

        getLogger().log(
                Level.INFO,
                "Player {0}@{1} disconnected",
                new Object[]{getUsername(service_name), getClientAddress(service_name)});

        removeService(service_name);
    }

    public void start() {
        try {
            create();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    while (running) {
                        try {
                            Socket client = server.accept();
                            acceptClient(client);
                        } catch (IOException ex) {
                            getLogger().log(Level.SEVERE, null, ex);
                        }
                    }
                }

            }, "server-thread").start();
            getLogger().info("Server Started");
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    private void acceptClient(Socket client) {
        String service_name = createServiceName();
        try {
            PlayerService service = new PlayerService(this, client, service_name);
            services.put(service_name, service);
            service.create();
            service.start();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "While creating ''{0}'' : {1}", new Object[]{service_name, ex.toString()});
        }
    }

    private String createServiceName() {
        return TAG_SERVICE + (current_service_number++);
    }

}
