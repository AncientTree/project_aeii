package com.toyknight.aeii.net.server;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.toyknight.aeii.AEIIException;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.manager.events.GameEvent;
import com.toyknight.aeii.net.Notification;
import com.toyknight.aeii.net.Request;
import com.toyknight.aeii.net.Response;
import com.toyknight.aeii.serializable.PlayerSnapshot;
import com.toyknight.aeii.serializable.RoomConfiguration;
import com.toyknight.aeii.serializable.RoomSnapshot;
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

    public boolean isRoomOpen(Room room) {
        return room != null && room.isOpen();
    }

    public boolean isRoomAvailable(Room room) {
        return room != null && room.getRemaining() > 0 && room.getHostPlayer() != -1;
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

    public PlayerSnapshot getPlayerSnapshot(int id) {
        PlayerService player = getPlayer(id);
        if (player == null) {
            return null;
        } else {
            PlayerSnapshot snapshot = new PlayerSnapshot();
            snapshot.id = id;
            snapshot.username = getPlayer(id).getUsername();
            return snapshot;
        }
    }

    public RoomConfiguration getRoomConfiguration(Room room) {
        RoomConfiguration configuration = new RoomConfiguration();
        configuration.room_number = room.getRoomNumber();
        configuration.host = room.getHostPlayer();
        configuration.game = room.getGame();
        configuration.team_allocation = room.getTeamAllocation();
        configuration.initial_gold = room.getInitialGold();
        configuration.max_population = room.getMaxPopulation();
        ObjectSet<Integer> players = room.getPlayers();
        configuration.players = new Array<PlayerSnapshot>();
        for (int id : players) {
            PlayerSnapshot snapshot = getPlayerSnapshot(id);
            if (snapshot != null) {
                snapshot.is_host = room.getHostPlayer() == id;
                configuration.players.add(snapshot);
            }
        }
        return configuration;
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
        onPlayerLeaveRoom(player.getID(), player.getRoomNumber());
        removePlayer(connection.getID());
        getLogger().log(
                Level.INFO,
                "Player {0}@{1} disconnected",
                new Object[]{username, address});
    }

    public void onReceive(Connection connection, Object object) {
        ServiceTask task = new ServiceTask(getPlayer(connection.getID()), object);
        executor.submit(task);
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
            case Request.JOIN_ROOM:
                doRespondJoinRoom(player, request);
                break;
            case Request.START_GAME:
                doRespondStartGame(player, request);
            default:
                //do nothing
        }
    }

    public void onReceiveNotification(PlayerService player, Notification notification) {
        switch (notification.getType()) {
            case Notification.PLAYER_LEAVING:
                onPlayerLeaveRoom(player.getID(), player.getRoomNumber());
                break;
            case Notification.UPDATE_ALLOCATION:
                onAllocationUpdate(player, notification);
                break;
            case Notification.UPDATE_ALLIANCE:
                onAllianceUpdate(player, notification);
                break;
            case Notification.GAME_EVENT:
                onSubmitGameEvent(player, notification);
                break;
            case Notification.MESSAGE:
                onSubmitMessage(player, notification);
                break;
            default:
                //do nothing
        }
    }

    public void doAuthentication(PlayerService player, Request request) {
        Response response = new Response(request.getID());

        String username = (String) request.getParameter(0);
        String v_string = (String) request.getParameter(1);

        if (V_STRING.equals(v_string)) {
            player.setAuthenticated(true);
            player.setUsername(username);
            response.setParameters(true, player.getID());
        } else {
            response.setParameters(false);
        }
        player.getConnection().sendTCP(response);
    }

    public void doRespondRoomList(PlayerService player, Request request) {
        if (player.isAuthenticated()) {
            Response response = new Response(request.getID());
            Array<RoomSnapshot> snapshots = getRoomsSnapshots();
            response.setParameters(snapshots);
            player.getConnection().sendTCP(response);
        }
    }

    public void doRespondCreateRoom(PlayerService player, Request request) {
        Response response = new Response(request.getID());
        if (player.isAuthenticated()) {
            Room room = new Room(current_room_number++, player.getUsername() + "'s game");
            room.setMap((Map) request.getParameter(1), (String) request.getParameter(0));
            room.setCapacity((Integer) request.getParameter(2));
            room.setInitialGold((Integer) request.getParameter(3));
            room.setMaxPopulation((Integer) request.getParameter(4));
            room.setHostPlayer(player.getID());
            room.addPlayer(player.getID());
            addRoom(room);
            player.setRoomNumber(room.getRoomNumber());

            RoomConfiguration configuration = getRoomConfiguration(room);
            response.setParameters(configuration);
            player.getConnection().sendTCP(response);
        }
    }

    public void doRespondJoinRoom(PlayerService player, Request request) {
        if (player.isAuthenticated()) {
            Response response = new Response(request.getID());
            long room_number = (Long) request.getParameter(0);
            Room room = getRoom(room_number);
            if (isRoomAvailable(room) && player.getRoomNumber() == -1) {
                getRoom(room_number).addPlayer(player.getID());
                player.setRoomNumber(room_number);

                notifyPlayerJoin(room, player.getID(), player.getUsername());

                RoomConfiguration configuration = getRoomConfiguration(room);
                response.setParameters(configuration);
            }
            player.getConnection().sendTCP(response);
        }
    }

    public void doRespondStartGame(PlayerService player, Request request) {
        if (player.isAuthenticated()) {
            Response response = new Response(request.getID());
            Room room = getRoom(player.getRoomNumber());
            if (isRoomOpen(room) && room.isReady() && room.getHostPlayer() == player.getID()) {
                room.startGame();
                response.setParameters(true);
            } else {
                response.setParameters(false);
            }
            notifyGameStart(room, player.getID());
            player.getConnection().sendTCP(response);
        }
    }

    public void onPlayerLeaveRoom(int leaver_id, long room_number) {
        PlayerService leaver = getPlayer(leaver_id);
        if (room_number >= 0 && leaver.getRoomNumber() == room_number) {
            Room room = getRoom(room_number);
            if (room != null) {
                room.removePlayer(leaver_id);
                leaver.setRoomNumber(-1);
                getLogger().log(
                        Level.INFO,
                        "Player {0}@{1} leaves room-{2}",
                        new Object[]{leaver.getUsername(), leaver.getAddress(), room_number});
                if (room.getCapacity() == room.getRemaining()) {
                    removeRoom(room_number);
                } else {
                    notifyPlayerLeave(room, leaver.getID(), leaver.getUsername());
                }
            }
        }
    }

    public void onAllocationUpdate(PlayerService updater, Notification notification) {
        if (updater.isAuthenticated()) {
            Integer[] allocation = (Integer[]) notification.getParameter(0);
            Integer[] types = (Integer[]) notification.getParameter(1);
            Room room = getRoom(updater.getRoomNumber());
            if (isRoomOpen(room) && room.getHostPlayer() == updater.getID()) {
                for (int team = 0; team < 4; team++) {
                    room.setTeamAllocation(team, allocation[team]);
                    room.setPlayerType(team, types[team]);
                }
                notifyAllocationUpdate(room, updater.getID(), allocation, types);
            }
        }
    }

    public void onAllianceUpdate(PlayerService updater, Notification notification) {
        if (updater.isAuthenticated()) {
            Integer[] alliance = (Integer[]) notification.getParameter(0);
            Room room = getRoom(updater.getRoomNumber());
            if (isRoomOpen(room) && room.getHostPlayer() == updater.getID()) {
                for (int team = 0; team < 4; team++) {
                    room.setAlliance(team, alliance[team]);
                }
                notifyAllianceUpdate(room, updater.getID(), alliance);
            }
        }
    }

    public void onSubmitGameEvent(PlayerService submitter, Notification notification) {
        if (submitter.isAuthenticated()) {
            try {
                GameEvent event = (GameEvent) notification.getParameter(0);
                Room room = getRoom(submitter.getRoomNumber());
                if (!room.isOpen()) {
//                    room.getManager().executeGameEvent(event, false);
//                    room.getManager().clearAnimations();
                    notifyGameEvent(room, submitter.getID(), event);
                }
            } catch (Exception ex) {
                System.err.println(ex.toString());
            }
        }
    }

    public void onSubmitMessage(PlayerService submitter, Notification notification) {
        if (submitter.isAuthenticated()) {
            String message = (String) notification.getParameter(0);
            Room room = getRoom(submitter.getRoomNumber());
            notifyMessage(room, submitter.getUsername(), message);
        }
    }

    public void notifyPlayerJoin(Room room, int joiner_id, String username) {
        Notification notification = new Notification(Notification.PLAYER_JOINING);
        notification.setParameters(joiner_id, username);
        for (int player_id : room.getPlayers()) {
            PlayerService player = getPlayer(player_id);
            if (player != null && joiner_id != player_id) {
                NotificationTask task = new NotificationTask(player.getConnection(), notification);
                executor.submit(task);
            }
        }
    }

    public void notifyPlayerLeave(Room room, int leaver_id, String username) {
        Notification notification = new Notification(Notification.PLAYER_LEAVING);
        notification.setParameters(leaver_id, username);
        for (int player_id : room.getPlayers()) {
            PlayerService player = getPlayer(player_id);
            if (player != null && leaver_id != player_id) {
                NotificationTask task = new NotificationTask(player.getConnection(), notification);
                executor.submit(task);
            }
        }
    }

    public void notifyAllocationUpdate(Room room, int updater_id, Integer[] allocation, Integer[] types) {
        Notification notification = new Notification(Notification.UPDATE_ALLOCATION);
        notification.setParameters(allocation, types);
        for (int player_id : room.getPlayers()) {
            PlayerService player = getPlayer(player_id);
            if (player != null && player_id != updater_id) {
                NotificationTask task = new NotificationTask(player.getConnection(), notification);
                executor.submit(task);
            }
        }
    }

    public void notifyAllianceUpdate(Room room, int updater_id, Integer[] alliance) {
        Notification notification = new Notification(Notification.UPDATE_ALLIANCE);
        notification.setParameters((Object) alliance);
        for (int player_id : room.getPlayers()) {
            PlayerService player = getPlayer(player_id);
            if (player != null && player_id != updater_id) {
                NotificationTask task = new NotificationTask(player.getConnection(), notification);
                executor.submit(task);
            }
        }
    }

    public void notifyGameStart(Room room, int starter_id) {
        Notification notification = new Notification(Notification.GAME_START);
        for (int player_id : room.getPlayers()) {
            PlayerService player = getPlayer(player_id);
            if (player != null && player_id != starter_id) {
                NotificationTask task = new NotificationTask(player.getConnection(), notification);
                executor.submit(task);
            }
        }
    }

    public void notifyGameEvent(Room room, int submitter_id, GameEvent event) {
        Notification notification = new Notification(Notification.GAME_EVENT);
        notification.setParameters(event);
        for (int player_id : room.getPlayers()) {
            PlayerService player = getPlayer(player_id);
            if (player != null && player_id != submitter_id) {
                NotificationTask task = new NotificationTask(player.getConnection(), notification);
                executor.submit(task);
            }
        }
    }

    public void notifyMessage(Room room, String username, String message) {
        Notification notification = new Notification(Notification.MESSAGE);
        notification.setParameters(username, message);
        for (int player_id : room.getPlayers()) {
            PlayerService player = getPlayer(player_id);
            if (player != null) {
                NotificationTask task = new NotificationTask(player.getConnection(), notification);
                executor.submit(task);
            }
        }
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

    private class ServiceTask implements Runnable {

        private final PlayerService player;
        private final Object received_obj;

        public ServiceTask(PlayerService player, Object received_obj) {
            this.player = player;
            this.received_obj = received_obj;
        }

        @Override
        public void run() {
            if (received_obj instanceof Request) {
                onReceiveRequest(player, (Request) received_obj);
            }
            if (received_obj instanceof Notification) {
                onReceiveNotification(player, (Notification) received_obj);
            }
        }

    }

    private class NotificationTask implements Runnable {

        private final Connection connection;
        private final Notification notification;

        public NotificationTask(Connection connection, Notification notification) {
            this.connection = connection;
            this.notification = notification;
        }

        @Override
        public void run() {
            connection.sendTCP(notification);
        }

    }

}
