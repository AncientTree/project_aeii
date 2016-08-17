//package net.toyknight.aeii.server.old;
//
//import com.badlogic.gdx.utils.Array;
//import com.badlogic.gdx.utils.ObjectMap;
//import com.badlogic.gdx.utils.ObjectSet;
//import com.esotericsoftware.kryonet.Connection;
//import com.esotericsoftware.kryonet.Listener;
//import com.esotericsoftware.kryonet.Server;
//import com.esotericsoftware.minlog.Log;
//import net.toyknight.aeii.AEIIException;
//import net.toyknight.aeii.GameContext;
//import net.toyknight.aeii.entity.GameCore;
//import net.toyknight.aeii.entity.Map;
//import net.toyknight.aeii.network.NetworkConstants;
//import net.toyknight.aeii.network.entity.PlayerSnapshot;
//import net.toyknight.aeii.network.entity.RoomSetting;
//import net.toyknight.aeii.network.entity.RoomSnapshot;
//import net.toyknight.aeii.server.RoomListener;
//import net.toyknight.aeii.server.entities.Player;
//import net.toyknight.aeii.server.entities.Room;
//import net.toyknight.aeii.server.managers.MapManager;
//import net.toyknight.aeii.utils.MD5Converter;
//import net.toyknight.aeii.utils.TileFactory;
//import net.toyknight.aeii.utils.UnitFactory;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * @author toyknight 10/27/2015.
// */
//public class GameServer implements RoomListener {
//
//    private final String TAG = "Server";
//
//    private String V_STRING;
//
//    private Server server;
//
//    private ExecutorService executor;
//
//    private MapManager map_manager;
//
//    private final Object PLAYER_LOCK = new Object();
//    private ObjectMap<Integer, Player> players;
//
//    private final Object ROOM_LOCK = new Object();
//    private ObjectMap<Long, Room> rooms;
//
//    public MapManager getMapManager() {
//        return map_manager;
//    }
//
//    public Player getPlayer(int id) {
//        synchronized (PLAYER_LOCK) {
//            return players.get(id);
//        }
//    }
//
//    public void removePlayer(int id) {
//        synchronized (PLAYER_LOCK) {
//            players.remove(id);
//        }
//    }
//
//    public boolean isRoomOpen(Room room) {
//        return room != null && room.isOpen();
//    }
//
//    public boolean isRoomAvailable(Room room) {
//        return room != null && !room.isGameOver() && room.getRemaining() > 0 && room.getHostID() != -1;
//    }
//
//    public Room getRoom(long room_number) {
//        synchronized (ROOM_LOCK) {
//            return rooms.get(room_number);
//        }
//    }
//
//    public void addRoom(Room room) {
//        synchronized (ROOM_LOCK) {
//            rooms.put(room.getRoomID(), room);
//        }
//    }
//
//    public void removeRoom(long room_number) {
//        synchronized (ROOM_LOCK) {
//            rooms.remove(room_number);
//        }
//        Log.info(TAG, String.format("Room [%d] is disposed", room_number));
//    }
//
//    public Array<RoomSnapshot> getRoomsSnapshots() {
//        synchronized (ROOM_LOCK) {
//            Array<RoomSnapshot> snapshots = new Array<RoomSnapshot>();
//            ObjectMap.Values<Room> room_list = rooms.values();
//            while (room_list.hasNext()) {
//                snapshots.add(room_list.next().createSnapshot());
//            }
//            return snapshots;
//        }
//    }
//
//    public RoomSetting createRoomSetting(Room room) {
//        RoomSetting room_setting = new RoomSetting();
//        room_setting.room_id = room.getRoomID();
//        room_setting.started = !room.isOpen();
//        room_setting.host = room.getHostID();
//        room_setting.allocation = room.getAllocation();
//        room_setting.start_gold = room.getStartGold();
//        room_setting.max_population = room.getUnitCapacity();
//        ObjectSet<Integer> players = room.getPlayers();
//        room_setting.players = new Array<PlayerSnapshot>();
//        room_setting.game = room.getGameCopy();
//        for (int id : players) {
//            Player player = getPlayer(id);
//            if (player != null) {
//                PlayerSnapshot snapshot = player.createSnapshot();
//                snapshot.is_host = room.getHostID() == id;
//                room_setting.players.add(snapshot);
//            }
//        }
//        return room_setting;
//    }
//
//    public void onPlayerConnect(Connection connection) {
//        int id = connection.getID();
//        Player player = new Player(connection);
//        players.put(id, player);
//    }
//
//    public void onPlayerDisconnect(Connection connection) {
//        Player player = getPlayer(connection.getID());
//        if (player != null) {
//            String username = player.getUsername();
//            String address = player.getAddress();
//            onPlayerLeaveRoom(player.getID(), player.getRoomID());
//            removePlayer(connection.getID());
//            Log.info(TAG, String.format("%s@%s disconnected", username, address));
//        }
//    }
//
//    public void onReceive(Connection connection, Object object) {
//        if (object instanceof String) {
//            ServiceTask task = new ServiceTask(getPlayer(connection.getID()), (String) object);
//            executor.submit(task);
//        }
//    }
//
//    public void onReceiveRequest(Player player, JSONObject request) throws JSONException {
//        switch (request.getInt("operation")) {
//            case NetworkConstants.AUTHENTICATION:
//                doAuthentication(player, request);
//                break;
//            case NetworkConstants.LIST_ROOMS:
//                doRespondRoomList(player, request);
//                break;
//            case NetworkConstants.CREATE_ROOM:
//                doRespondCreateRoomNewGame(player, request);
//                break;
//            case NetworkConstants.JOIN_ROOM:
//                doRespondJoinRoom(player, request);
//                break;
//            case NetworkConstants.START_GAME:
//                doRespondStartGame(player, request);
//                break;
//            case NetworkConstants.CREATE_ROOM_SAVED:
//                doRespondCreateRoomSavedGame(player, request);
//                break;
//            case NetworkConstants.GAME_EVENT:
//                onSubmitGameEvent(player, request);
//                break;
//            case NetworkConstants.LIST_MAPS:
//                doRespondListMaps(player, request);
//                break;
//            case NetworkConstants.UPLOAD_MAP:
//                doRespondUploadMap(player, request);
//                break;
//            case NetworkConstants.DOWNLOAD_MAP:
//                doRespondDownloadMap(player, request);
//                break;
//            default:
//                //do nothing
//        }
//    }
//
//    public void onReceiveNotification(Player player, JSONObject notification) throws JSONException {
//        switch (notification.getInt("operation")) {
//            case NetworkConstants.PLAYER_LEAVING:
//                onPlayerLeaveRoom(player.getID(), player.getRoomID());
//                break;
//            case NetworkConstants.UPDATE_ALLOCATION:
//                onAllocationUpdate(player, notification);
//                break;
////            case NetworkConstants.GAME_EVENT:
////                onSubmitGameEvent(player, notification);
////                break;
//            case NetworkConstants.MESSAGE:
//                onSubmitMessage(player, notification);
//                break;
//            default:
//                //do nothing
//        }
//    }
//
//    public void doAuthentication(Player player, JSONObject request) {
//        try {
//            JSONObject response = createResponse(request);
//
//            String username = request.getString("username");
//            String v_string = request.getString("v_string");
//
//            if (V_STRING.equals(v_string)) {
//                player.setAuthenticated(true);
//                player.setUsername(username);
//                response.put("approved", true);
//                response.put("service_id", player.getID());
//                Log.info(TAG, String.format("%s@%s authenticated.", player.getUsername(), player.getAddress()));
//            } else {
//                response.put("approved", false);
//                Log.info(TAG, String.format("%s@%s authentication failed.", username, player.getAddress()));
//            }
//            player.sendTCP(response.toString());
//        } catch (JSONException ex) {
//            String message = String.format(
//                    "Bad authentication request from %s@%s", player.getUsername(), player.getAddress());
//            Log.error(TAG, message, ex);
//        }
//    }
//
//    public void doRespondRoomList(Player player, JSONObject request) {
//        try {
//            if (player.isAuthenticated()) {
//                JSONObject response = createResponse(request);
//                JSONArray rooms = new JSONArray();
//                for (RoomSnapshot snapshot : getRoomsSnapshots()) {
//                    rooms.put(snapshot.toJson());
//                }
//                response.put("rooms", rooms);
//                player.sendTCP(response.toString());
//            }
//        } catch (JSONException ex) {
//            String message = String.format(
//                    "Bad room listing request from %s@%s", player.getUsername(), player.getAddress());
//            Log.error(TAG, message, ex);
//        }
//    }
//
//    public void doRespondCreateRoomNewGame(Player player, JSONObject request) {
//        try {
//            if (player.isAuthenticated()) {
//                JSONObject response = createResponse(request);
//                Room room = new Room(this, System.currentTimeMillis(), player.getUsername() + "'s game");
//                room.initialize(new Map(request.getJSONObject("map")));
//                if (request.has("password")) {
//                    room.setPassword(request.getString("password"));
//                }
//                room.setMapName(request.getString("map_name"));
//                room.setPlayerCapacity(request.getInt("capacity"));
//                room.setStartGold(request.getInt("start_gold"));
//                room.setUnitCapacity(request.getInt("max_population"));
//                room.setHostPlayer(player.getID());
//                room.addPlayer(player.getID());
//                addRoom(room);
//                player.setRoomID(room.getRoomID());
//
//                RoomSetting room_setting = createRoomSetting(room);
//                response.put("room_setting", room_setting.toJson());
//                response.put("approved", true);
//
//                Log.info(TAG, String.format(
//                        "%s@%s creates room [%d]", player.getUsername(), player.getAddress(), room.getRoomID()));
//
//                player.sendTCP(response.toString());
//            }
//        } catch (JSONException ex) {
//            String message = String.format(
//                    "Bad room creating [new] request from %s@%s", player.getUsername(), player.getAddress());
//            Log.error(TAG, message, ex);
//        }
//    }
//
//    public void doRespondCreateRoomSavedGame(Player player, JSONObject request) {
//        try {
//            if (player.isAuthenticated()) {
//                JSONObject response = createResponse(request);
//                GameCore game = new GameCore(request.getJSONObject("game"));
//                Room room = new Room(this, System.currentTimeMillis(), player.getUsername() + "'s game", game);
//                if (request.has("password")) {
//                    room.setPassword(request.getString("password"));
//                }
//                room.setMapName(request.getString("map_name"));
//                room.setPlayerCapacity(request.getInt("capacity"));
//                room.setHostPlayer(player.getID());
//                room.addPlayer(player.getID());
//                addRoom(room);
//                player.setRoomID(room.getRoomID());
//
//                RoomSetting room_setting = createRoomSetting(room);
//                response.put("room_setting", room_setting.toJson());
//                response.put("approved", true);
//
//                Log.info(TAG, String.format(
//                        "%s@%s creates room [%d]", player.getUsername(), player.getAddress(), room.getRoomID()));
//
//                player.sendTCP(response.toString());
//            }
//        } catch (JSONException ex) {
//            String message = String.format(
//                    "Bad room creating [save] request from %s@%s", player.getUsername(), player.getAddress());
//            Log.error(TAG, message, ex);
//        }
//    }
//
//    public void doRespondJoinRoom(Player player, JSONObject request) {
//        try {
//            if (player.isAuthenticated()) {
//                JSONObject response = createResponse(request);
//                long room_number = request.getLong("room_id");
//                Room room = getRoom(room_number);
//                if (isRoomAvailable(room)
//                        && player.getRoomID() == -1
//                        && room.checkPassword(request.getString("password"))) {
//                    room.addPlayer(player.getID());
//                    player.setRoomID(room_number);
//                    RoomSetting room_setting = createRoomSetting(room);
//                    response.put("room_setting", room_setting.toJson());
//                    response.put("approved", true);
//                    player.sendTCP(response.toString());
//                    notifyPlayerJoin(room, player.getID(), player.getUsername());
//                } else {
//                    response.put("approved", false);
//                    player.sendTCP(response.toString());
//                }
//            }
//        } catch (JSONException ex) {
//            String message = String.format(
//                    "Bad room joining request from %s@%s", player.getUsername(), player.getAddress());
//            Log.error(TAG, message, ex);
//        }
//    }
//
//    public void doRespondStartGame(Player player, JSONObject request) {
//        try {
//            if (player.isAuthenticated()) {
//                JSONObject response = createResponse(request);
//                Room room = getRoom(player.getRoomID());
//                if (isRoomOpen(room) && room.isReady() && room.getHostID() == player.getID()) {
//                    room.startGame();
//                    response.put("approved", true);
//                    notifyGameStart(room, player.getID());
//                } else {
//                    response.put("approved", false);
//                }
//                player.sendTCP(response.toString());
//            }
//        } catch (JSONException ex) {
//            String message = String.format(
//                    "Bad game starting request from %s@%s", player.getUsername(), player.getAddress());
//            Log.error(TAG, message, ex);
//        }
//    }
//
//    public void doRespondListMaps(Player player, JSONObject request) {
//        try {
//            JSONObject response = createResponse(request);
//            if (request.has("author")) {
//                String author = request.getString("author");
//                response.put("maps", getMapManager().getSerializedMapList(author));
//            } else {
//                response.put("maps", getMapManager().getSerializedAuthorList());
//            }
//            player.sendTCP(response.toString());
//        } catch (JSONException ex) {
//            String message = String.format("Bad map listing request from %s", player.getAddress());
//            Log.error(TAG, message, ex);
//        }
//    }
//
//    public void doRespondUploadMap(Player player, JSONObject request) {
//        try {
//            JSONObject response = createResponse(request);
//            Map map = new Map(request.getJSONObject("map"));
//            String map_name = request.getString("map_name");
//            boolean approved;
//            try {
//                getMapManager().addMap(map, map_name);
//                approved = true;
//            } catch (IOException ex) {
//                approved = false;
//            }
//            response.put("approved", approved);
//            player.sendTCP(response.toString());
//        } catch (JSONException ex) {
//            String message = String.format("Bad map uploading request from %s", player.getAddress());
//            Log.error(TAG, message, ex);
//        }
//    }
//
//    public void doRespondDownloadMap(Player player, JSONObject request) {
//        try {
//            JSONObject response = createResponse(request);
//            String filename = request.getString("filename");
//            boolean approved;
//            try {
//                Map map = getMapManager().getMap(filename);
//                response.put("map", map.toJson());
//                approved = true;
//            } catch (IOException ex) {
//                approved = false;
//            } catch (AEIIException ex) {
//                approved = false;
//            }
//            response.put("approved", approved);
//            player.sendTCP(response.toString());
//        } catch (JSONException ex) {
//            String message = String.format("Bad map downloading request from %s", player.getAddress());
//            Log.error(TAG, message, ex);
//        }
//    }
//
//    public void onPlayerLeaveRoom(int leaver_id, long room_number) {
//        Player leaver = getPlayer(leaver_id);
//        if (room_number >= 0 && leaver.getRoomID() == room_number) {
//            Room room = getRoom(room_number);
//            if (room != null) {
//                room.removePlayer(leaver_id);
//                leaver.setRoomID(-1);
//                Log.info(TAG, String.format(
//                        "%s@%s leaves room [%d]", leaver.getUsername(), leaver.getAddress(), room_number));
//                if (room.getPlayerCapacity() == room.getRemaining()) {
//                    room.dispose();
//                    removeRoom(room_number);
//                } else {
//                    notifyPlayerLeave(room, leaver.getID(), leaver.getUsername(), room.getHostID());
//                    JSONArray types = new JSONArray();
//                    JSONArray alliance = new JSONArray();
//                    JSONArray allocation = new JSONArray();
//                    for (int team = 0; team < 4; team++) {
//                        types.put(room.getPlayerType(team));
//                        alliance.put(room.getAlliance(team));
//                        allocation.put(room.getAllocation(team));
//                    }
//                    notifyAllocationUpdate(room, -1, alliance, allocation, types);
//                }
//            }
//        }
//    }
//
//    public void onAllocationUpdate(Player updater, JSONObject notification) {
//        try {
//            if (updater.isAuthenticated()) {
//                Room room = getRoom(updater.getRoomID());
//                if (room != null && room.getHostID() == updater.getID()) {
//                    JSONArray types = notification.getJSONArray("types");
//                    JSONArray alliance = notification.getJSONArray("alliance");
//                    JSONArray allocation = notification.getJSONArray("allocation");
//                    for (int team = 0; team < 4; team++) {
//                        room.setPlayerType(team, types.getInt(team));
//                        room.setAlliance(team, alliance.getInt(team));
//                        room.setAllocation(team, allocation.getInt(team));
//                    }
//                    notifyAllocationUpdate(room, updater.getID(), alliance, allocation, types);
//                }
//            }
//        } catch (JSONException ex) {
//            String message = String.format(
//                    "Bad allocation updating notification from %s@%s", updater.getUsername(), updater.getAddress());
//            Log.error(TAG, message, ex);
//        }
//    }
//
//    public void onSubmitGameEvent(Player submitter, JSONObject request) {
//        try {
//            JSONObject event = request.getJSONObject("game_event");
//            if (submitter.isAuthenticated()) {
//                Room room = getRoom(submitter.getRoomID());
//                if (!room.isOpen()/* && submitter.getID() == room.getCurrentPlayerID()*/) {
//                    room.submitGameEvent(event, submitter.getID());
//                    JSONObject response = createResponse(request);
//                    submitter.sendTCP(response.toString());
//                }
//            }
//        } catch (JSONException ex) {
//            String message = String.format(
//                    "Bad game event from %s@%s", submitter.getUsername(), submitter.getAddress());
//            Log.error(TAG, message, ex);
//        }
//    }
//
//    public void onSubmitMessage(Player submitter, JSONObject notification) {
//        try {
//            if (submitter.isAuthenticated()) {
//                String message = notification.getString("message");
//                Room room = getRoom(submitter.getRoomID());
//                if (!message.startsWith("/")) {
//                    notifyMessage(room, submitter.getUsername(), message);
//                }
//            }
//        } catch (JSONException ex) {
//            String message = String.format(
//                    "Bad message notification from %s@%s", submitter.getUsername(), submitter.getAddress());
//            Log.error(TAG, message, ex);
//        }
//    }
//
//    public void notifyPlayerJoin(Room room, int joiner_id, String username) {
//        try {
//            for (int player_id : room.getPlayers()) {
//                Player player = getPlayer(player_id);
//                if (player != null && joiner_id != player_id) {
//                    JSONObject notification = createNotification(NetworkConstants.PLAYER_JOINING);
//                    notification.put("player_id", joiner_id);
//                    notification.put("username", username);
//                    sendNotification(player, notification);
//                }
//            }
//        } catch (JSONException ex) {
//            Log.error(TAG, "An error occurred while notifying player join", ex);
//        }
//    }
//
//    public void notifyPlayerLeave(Room room, int leaver_id, String username, int host_id) {
//        try {
//            for (int player_id : room.getPlayers()) {
//                Player player = getPlayer(player_id);
//                if (player != null && leaver_id != player_id) {
//                    JSONObject notification = createNotification(NetworkConstants.PLAYER_LEAVING);
//                    notification.put("player_id", leaver_id);
//                    notification.put("username", username);
//                    notification.put("host_id", host_id);
//                    sendNotification(player, notification);
//                }
//            }
//        } catch (JSONException ex) {
//            Log.error(TAG, "An error occurred while notifying player leave", ex);
//        }
//    }
//
//    public void notifyAllocationUpdate(
//            Room room, int updater_id, JSONArray alliance, JSONArray allocation, JSONArray types) {
//        try {
//            for (int player_id : room.getPlayers()) {
//                Player player = getPlayer(player_id);
//                if (player != null && player_id != updater_id) {
//                    JSONObject notification = createNotification(NetworkConstants.UPDATE_ALLOCATION);
//                    notification.put("types", types);
//                    notification.put("alliance", alliance);
//                    notification.put("allocation", allocation);
//                    sendNotification(player, notification);
//                }
//            }
//        } catch (JSONException ex) {
//            Log.error(TAG, "An error occurred while notifying allocation update", ex);
//        }
//    }
//
//    public void notifyGameStart(Room room, int starter_id) {
//        try {
//            for (int player_id : room.getPlayers()) {
//                Player player = getPlayer(player_id);
//                if (player != null && player_id != starter_id) {
//                    JSONObject notification = createNotification(NetworkConstants.GAME_START);
//                    sendNotification(player, notification);
//                }
//            }
//        } catch (JSONException ex) {
//            Log.error(TAG, "An error occurred while notifying game start", ex);
//        }
//    }
//
//    public void notifyGameEvent(Room room, int submitter_id, JSONObject event) {
//        try {
//            for (int player_id : room.getPlayers()) {
//                Player player = getPlayer(player_id);
//                if (player != null && player_id != submitter_id) {
//                    JSONObject notification = createNotification(NetworkConstants.GAME_EVENT);
//                    notification.put("game_event", event);
//                    player.sendTCP(notification.toString());
//                }
//            }
//        } catch (JSONException ex) {
//            Log.error(TAG, "An error occurred while notifying game event", ex);
//        }
//    }
//
//    public void notifyMessage(Room room, String username, String message) {
//        try {
//            for (int player_id : room.getPlayers()) {
//                Player player = getPlayer(player_id);
//                if (player != null) {
//                    JSONObject notification = createNotification(NetworkConstants.MESSAGE);
//                    notification.put("username", username);
//                    notification.put("message", message);
//                    sendNotification(player, notification);
//                }
//            }
//        } catch (JSONException ex) {
//            Log.error(TAG, "An error occurred while notifying message", ex);
//        }
//    }
//
//    @Override
//    public void onGameEventExecuted(Room room, JSONObject event, int player_id) {
//        notifyGameEvent(room, player_id, event);
//    }
//
//    @Override
//    public void onCheatingDetected(Room room, int player_id, Throwable cause) {
//        Player player = getPlayer(player_id);
//        if (player != null) {
//            String message = String.format(
//                    "Player %s@%s was detected cheating", player.getUsername(), player.getAddress());
//            Log.info(TAG, message, cause);
//            DisconnectingTask disconnecting_task = new DisconnectingTask(player, "/cheating");
//            executor.submit(disconnecting_task);
//        }
//    }
//
//    public void sendNotification(Player player, JSONObject notification) {
//        NotificationTask task = new NotificationTask(player, notification);
//        executor.submit(task);
//    }
//
//    public String getVerificationString() {
//        String V_STRING =
//                TileFactory.getVerificationString() + UnitFactory.getVerificationString() + GameContext.INTERNAL_VERSION;
//        return new MD5Converter().toMD5(V_STRING);
//    }
//
//    private void create() throws AEIIException {
//        players = new ObjectMap<Integer, Player>();
//        rooms = new ObjectMap<Long, Room>();
//
//        UnitFactory.loadUnitData();
//        TileFactory.loadTileData();
//
//        V_STRING = getVerificationString();
//
//        map_manager = new MapManager();
//        map_manager.initialize();
//
//        executor = Executors.newFixedThreadPool(64);
//
//        server = new Server(65536, 65536);
//        server.addListener(new Listener() {
//            @Override
//            public void connected(Connection connection) {
//                onPlayerConnect(connection);
//            }
//
//            @Override
//            public void disconnected(Connection connection) {
//                onPlayerDisconnect(connection);
//            }
//
//            @Override
//            public void received(Connection connection, Object object) {
//                onReceive(connection, object);
//            }
//        });
//    }
//
//    public void start() {
//        try {
//            create();
//            server.start();
//            server.bind(5438);
//        } catch (IOException ex) {
//            Log.error(TAG, "An error occurred while starting the server", ex);
//        } catch (AEIIException ex) {
//            Log.error(TAG, "An error occurred while creating the server", ex);
//        }
//    }
//
//    private JSONObject createResponse(JSONObject request) throws JSONException {
//        JSONObject response = new JSONObject();
//        response.put("request_id", request.getLong("request_id"));
//        response.put("type", NetworkConstants.RESPONSE);
//        return response;
//    }
//
//    private JSONObject createNotification(int operation) throws JSONException {
//        JSONObject notification = new JSONObject();
//        notification.put("type", NetworkConstants.NOTIFICATION);
//        notification.put("operation", operation);
//        return notification;
//    }
//
//    private class ServiceTask implements Runnable {
//
//        private final Player player;
//        private final String packet_content;
//
//        public ServiceTask(Player player, String packet_content) {
//            this.player = player;
//            this.packet_content = packet_content;
//        }
//
//        @Override
//        public void run() {
//            try {
//                JSONObject packet = new JSONObject(packet_content);
//                switch (packet.getInt("type")) {
//                    case NetworkConstants.REQUEST:
//                        onReceiveRequest(player, packet);
//                        break;
//                    case NetworkConstants.NOTIFICATION:
//                        onReceiveNotification(player, packet);
//                        break;
//                }
//            } catch (JSONException ex) {
//                String message = String.format(
//                        "An error occurred while processing packet from %s@%s",
//                        player.getUsername(), player.getAddress());
//                Log.error(TAG, message, ex);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//
//    }
//
//    private class NotificationTask implements Runnable {
//
//        private final Player player;
//        private final JSONObject notification;
//
//        public NotificationTask(Player player, JSONObject notification) {
//            this.player = player;
//            this.notification = notification;
//        }
//
//        @Override
//        public void run() {
//            player.sendTCP(notification.toString());
//        }
//
//    }
//
//    private class DisconnectingTask implements Runnable {
//
//        private final Player player;
//        private final String message;
//
//        public DisconnectingTask(Player player, String message) {
//            this.player = player;
//            this.message = message;
//        }
//
//        @Override
//        public void run() {
//            JSONObject notification = createNotification(NetworkConstants.MESSAGE);
//            notification.put("username", "Server");
//            notification.put("message", message);
//            player.sendTCP(notification.toString());
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException ignored) {
//            }
//            player.getConnection().close();
//        }
//
//    }
//
//}
