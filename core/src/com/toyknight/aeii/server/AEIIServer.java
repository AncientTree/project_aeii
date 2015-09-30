package com.toyknight.aeii.server;

import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.serializable.GameSave;
import com.toyknight.aeii.serializable.PlayerSnapshot;
import com.toyknight.aeii.server.entity.Room;
import com.toyknight.aeii.serializable.RoomConfig;
import com.toyknight.aeii.serializable.RoomSnapshot;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
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

    private Properties config;

    private boolean running;
    private ServerSocket server;
    private ThreadGroup service_group;

    private long current_service_number;
    private HashMap<String, PlayerService> services;

    private long current_room_number;
    private HashMap<Long, Room> rooms;

    private void create() throws IOException {
        loadConfiguration();

        running = true;
        current_service_number = 0;

        service_group = new ThreadGroup("service-group");
        server = new ServerSocket(5438);

        services = new HashMap<String, PlayerService>();

        current_room_number = 0;
        rooms = new HashMap<Long, Room>();
    }

    private void loadConfiguration() {
        config = new Properties();
        File config_file = new File("server.cfg");
        if (config_file.exists() && config_file.isFile()) {
            try {
                FileInputStream fis = new FileInputStream(config_file);
                config.load(fis);
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, ex.toString());
                useDefaultConfiguration();
            }
        } else {
            useDefaultConfiguration();
        }
    }

    private void useDefaultConfiguration() {
        config.put("PASSWORD", "password");
        config.put("V_STRING", "560b0f614c5b0965e5a949e8ba770e61");
    }

    public boolean isRunning() {
        return running;
    }

    public String getVerificationString() {
        return getConfiguration().getProperty("V_STRING");
    }

    public Properties getConfiguration() {
        return config;
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
        Set<String> players = room.getPlayers();
        config.players = new PlayerSnapshot[players.size()];
        int index = 0;
        for (String service : players) {
            PlayerSnapshot snapshot = createPlayerSnapshot(service);
            snapshot.is_host = room.getHostService().equals(service);
            config.players[index++] = snapshot;
        }
        return config;
    }

    public PlayerSnapshot createPlayerSnapshot(String service_name) {
        PlayerSnapshot snapshot = new PlayerSnapshot();
        snapshot.service_name = service_name;
        snapshot.username = getService(service_name).getUsername();
        return snapshot;
    }

    public boolean isOpen(Room room) {
        return room != null && room.isOpen();
    }

    public void removeRoom(long room_number) {
        synchronized (ROOM_LOCK) {
            rooms.remove(room_number);
        }
        getLogger().log(Level.INFO, "Dispose room-{0}", room_number);
    }

    public String getUsername(String service_name) {
        return getService(service_name).getUsername();
    }

    public String getClientAddress(String service_name) {
        return getService(service_name).getClientAddress();
    }

    public ArrayList<RoomSnapshot> getRoomSnapshot() {
        ArrayList<RoomSnapshot> snapshot = new ArrayList<RoomSnapshot>();
        synchronized (ROOM_LOCK) {
            for (Room room : rooms.values()) {
                if (room.isOpen()) {
                    snapshot.add(room.createSnapshot());
                }
            }
        }
        return snapshot;
    }

    public RoomConfig onPlayerCreateRoom(String service_name, String map_name, Map map, int capacity, int gold, int population) {
        PlayerService player = getService(service_name);
        Room room = new Room(current_room_number++, player.getUsername() + "'s game");
        room.setMapName(map_name);
        room.setMap(map);
        room.setCapacity(capacity);
        room.setInitialGold(gold);
        room.setMaxPopulation(population);
        room.setHostService(service_name);
        room.addPlayer(service_name);
        player.setRoomNumber(room.getRoomNumber());
        rooms.put(room.getRoomNumber(), room);
        return createRoomConfig(room);
    }

    public RoomConfig onPlayerJoinRoom(String service_name, long room_number) {
        Room room = getRoom(room_number);
        if (isOpen(room) && room.getRemaining() > 0 && getService(service_name).getRoomNumber() == -1 && room.getHostService() != null) {
            room.addPlayer(service_name);
            getService(service_name).setRoomNumber(room_number);

            for (String player : room.getPlayers()) {
                PlayerService player_service = getService(player);
                if (player_service != null && !player.equals(service_name)) {
                    player_service.notifyPlayerJoining(service_name, getService(service_name).getUsername());
                    player_service.notifyAllocation(room.getTeamAllocation(), room.getPlayerType());
                }
            }

            return createRoomConfig(room);
        } else {
            return null;
        }
    }

    public GameCore onPlayerJoinStartedGame(String service_name, long room_number) {
        Room room = getRoom(room_number);
        if (room != null && room.getRemaining() > 0 && room.getHostService() != null) {
            room.addPlayer(service_name);
            getService(service_name).setRoomNumber(room_number);
            try {
                GameCore game = getService(room.getHostService()).requestGame();

                //notify player join
                for (String player : room.getPlayers()) {
                    PlayerService player_service = getService(player);
                    if (player_service != null && !player.equals(service_name)) {
                        player_service.notifyPlayerJoining(service_name, getService(service_name).getUsername());
                    }
                }

                return game;
            } catch (IOException e) {
                return null;
            } catch (ClassNotFoundException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public void onPlayerLeaveRoom(String leaver_service, String username) {
        PlayerService service = getService(leaver_service);
        long room_number = service.getRoomNumber();
        if (room_number >= 0) {
            Room room = getRoom(room_number);
            room.removePlayer(leaver_service);
            service.setRoomNumber(-1);
            getLogger().log(
                    Level.INFO,
                    "Player {0}@{1} leaves room-{2}",
                    new Object[]{getUsername(leaver_service), getClientAddress(leaver_service), room_number});
            if (room.getCapacity() == room.getRemaining()) {
                removeRoom(room_number);
            } else {
                for (String player : room.getPlayers()) {
                    PlayerService player_service = getService(player);
                    if (player_service != null) {
                        player_service.notifyPlayerLeaving(leaver_service, username);
                        player_service.notifyAllocation(room.getTeamAllocation(), room.getPlayerType());
                    }
                }
            }
        }
    }

    public void onUpdateAllocation(String service_name, String[] allocation, Integer[] types) {
        PlayerService requester = getService(service_name);
        if (requester != null) {
            Room room = getRoom(requester.getRoomNumber());
            if (isOpen(room) && room.getHostService().equals(service_name)) {
                for (int team = 0; team < 4; team++) {
                    room.setTeamAllocation(team, allocation[team]);
                    room.setPlayerType(team, types[team]);
                }
                for (String player : room.getPlayers()) {
                    PlayerService player_service = getService(player);
                    if (player_service != null && !player.equals(service_name)) {
                        player_service.notifyAllocation(allocation, types);
                    }
                }
            }
        }
    }

    public void onUpdateAlliance(String service_name, Integer[] alliance) {
        PlayerService requester = getService(service_name);
        if (requester != null) {
            Room room = getRoom(requester.getRoomNumber());
            if (isOpen(room) && room.getHostService().equals(service_name)) {
                for (int team = 0; team < 4; team++) {
                    room.setAlliance(team, alliance[team]);
                }
                for (String player : room.getPlayers()) {
                    PlayerService player_service = getService(player);
                    if (player_service != null && !player.equals(service_name)) {
                        player_service.notifyAlliance(alliance);
                    }
                }
            }
        }
    }

    public boolean onStartGame(long room_number, String requester) {
        Room room = getRoom(room_number);
        if (room == null || !room.getHostService().equals(requester) || !room.isReady()) {
            return false;
        } else {
            for (String service_name : room.getPlayers()) {
                if (!service_name.equals(requester)) {
                    getService(service_name).notifyGameStart();
                }
            }
            room.setGameStarted(true);
            return true;
        }
    }

    public boolean onResumeGame(long room_number, String requester, GameSave game_save) {
        Room room = getRoom(room_number);
        if (room == null || !room.getHostService().equals(requester) || !room.isReady()) {
            return false;
        } else {
            for (String service_name : room.getPlayers()) {
                if (!service_name.equals(requester)) {
                    getService(service_name).notifyGameResume(game_save);
                }
            }
            room.setGameStarted(true);
            return true;
        }
    }

    public boolean onSubmitGameEvent(String submitter, GameEvent event) throws IOException {
        Room room = getRoom(getService(submitter).getRoomNumber());
        if (room == null || room.isOpen()) {
            return false;
        } else {
            Set<String> players = room.getPlayers();
            for (String player_service : players) {
                PlayerService service = getService(player_service);
                if (!player_service.equals(submitter) && service != null) {
                    service.notifyGameEvent(event);
                }
            }
            return true;
        }
    }

    public void onSubmitMessage(String submitter_service, String message) {
        PlayerService submitter = getService(submitter_service);
        Room room = getRoom(submitter.getRoomNumber());
        if (room != null) {
            Set<String> players = room.getPlayers();
            for (String player_service : players) {
                PlayerService service = getService(player_service);
                if (service != null) {
                    service.notifyMessage(submitter.getUsername(), message);
                }
            }
        }
    }

    public void onPlayerDisconnect(String service_name) {
        String username = getService(service_name).getUsername();
        String address = getService(service_name).getClientAddress();
        onPlayerLeaveRoom(service_name, username);
        removeService(service_name);
        getLogger().log(
                Level.INFO,
                "Player {0}@{1} disconnected",
                new Object[]{username, address});
    }

    public void onShutdownRequested(String password) {
        String actual_password = getConfiguration().getProperty("PASSWORD");
        if (actual_password != null && actual_password.equals(password)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (SERVICE_LOCK) {
                        for (PlayerService service : services.values()) {
                            service.closeConnection();
                        }
                    }
                    running = false;
                    try {
                        server.close();
                    } catch (IOException ex) {
                        getLogger().log(Level.SEVERE, ex.toString());
                        System.exit(-1);
                    }
                }
            }, "shutdown-thread").start();
            getLogger().log(Level.INFO, "Shutdown server");
        }
    }

    public void start() {
        try {
            create();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    while (isRunning()) {
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
        String TAG_SERVICE = "service-";
        return TAG_SERVICE + (current_service_number++);
    }

}
