package net.toyknight.aeii.server.entities;

import static net.toyknight.aeii.entity.Rule.Entry.*;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.entity.Player;
import net.toyknight.aeii.manager.CheatingException;
import net.toyknight.aeii.manager.GameManager;
import net.toyknight.aeii.network.entity.RoomSnapshot;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.server.RoomListener;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author toyknight
 */
public class Room {

    public final Object GAME_LOCK = new Object();

    public final Object PLAYER_LOCK = new Object();

    private final ExecutorService event_executor = Executors.newSingleThreadExecutor();

    private final long room_id;
    private final String room_name;
    private final int start_gold;

    private RoomListener listener;

    private boolean game_started;

    private int capacity = 4;

    private int host_player_id;
    private final ObjectSet<Integer> players;
    private final int[] allocation;

    private GameManager manager;

    private String map_name;


    private String password = null;

    public Room(long room_id, String room_name, GameCore game) {
        this(room_id, room_name, -1);
        setGame(game);
    }

    public Room(long room_id, String room_name, Map map, int start_gold) {
        this(room_id, room_name, start_gold);
        Rule rule = Rule.createDefault();
        GameCore game = new GameCore(map, rule, 0, GameCore.SKIRMISH);
        for (int team = 0; team < 4; team++) {
            game.getPlayer(team).setAlliance(team + 1);
            game.getPlayer(team).setGold(start_gold);
        }
        setGame(game);
    }

    private Room(long room_id, String room_name, int start_gold) {
        this.room_id = room_id;
        this.room_name = room_name;
        this.start_gold = start_gold;
        this.game_started = false;
        this.players = new ObjectSet<Integer>();
        this.allocation = new int[4];
        Arrays.fill(allocation, -1);
        host_player_id = -1;
        game_started = false;
        manager = new GameManager();
        manager.getGameEventExecutor().setCheckEventValue(false);
    }

    public void setListener(RoomListener listener) {
        this.listener = listener;
    }

    public RoomListener getListener() {
        return listener;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean checkPassword(String password) {
        return this.password == null || this.password.equals(password);
    }

    public GameManager getManager() {
        return manager;
    }

    private void setGame(GameCore game) {
        getManager().setGame(game);
    }

    public GameCore getGame() {
        return getManager().getGame();
    }

    public GameCore getGameCopy() {
        synchronized (GAME_LOCK) {
            return new GameCore(getGame());
        }
    }

    public Map getMap() {
        return getGame().getMap();
    }

    public ObjectSet<Integer> getPlayers() {
        synchronized (PLAYER_LOCK) {
            return new ObjectSet<Integer>(players);
        }
    }

    public void addPlayer(int id) {
        synchronized (PLAYER_LOCK) {
            players.add(id);
        }
    }

    public void removePlayer(int id) {
        synchronized (PLAYER_LOCK) {
            players.remove(id);
            for (int team = 0; team < 4; team++) {
                if (allocation[team] == id) {
                    setPlayerType(team, Player.NONE);
                    allocation[team] = -1;
                }
            }
            if (host_player_id == id) {
                if (players.size > 0) {
                    host_player_id = players.first();
                } else {
                    host_player_id = -1;
                }
            }
        }
    }

    public int getCurrentPlayerID() {
        synchronized (GAME_LOCK) {
            return allocation[getGame().getCurrentTeam()];
        }
    }

    public void setHostPlayer(int id) {
        host_player_id = id;
    }

    public int getHostID() {
        return host_player_id;
    }

    public void setAllocation(int team, int id) {
        allocation[team] = id;
    }

    public int getAllocation(int team) {
        return allocation[team];
    }

    public int[] getAllocation() {
        return allocation;
    }

    public void setPlayerType(int team, int type) {
        synchronized (GAME_LOCK) {
            if (isOpen()) {
                Player player = getGame().getPlayer(team);
                if (getGame().getMap().hasTeamAccess(team)) {
                    player.setType(type);
                }
            }
        }
    }

    public int getPlayerType(int team) {
        return getGame().getPlayer(team).getType();
    }

    public void setAlliance(int team, int alliance) {
        synchronized (GAME_LOCK) {
            if (isOpen()) {
                getGame().getPlayer(team).setAlliance(alliance);
            }
        }
    }

    public int getAlliance(int team) {
        return getGame().getPlayer(team).getAlliance();
    }

    public long getRoomID() {
        return room_id;
    }

    public String getRoomName() {
        return room_name;
    }

    public void setPlayerCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getPlayerCapacity() {
        return capacity;
    }

    public int getRemaining() {
        return getPlayerCapacity() - players.size;
    }

    public void setMapName(String name) {
        this.map_name = name;
    }

    public String getMapName() {
        return map_name;
    }

    public int getStartGold() {
        return start_gold;
    }

    public void setUnitCapacity(int capacity) {
        synchronized (GAME_LOCK) {
            getGame().getRule().setValue(UNIT_CAPACITY, capacity);
        }
    }

    public int getUnitCapacity() {
        synchronized (GAME_LOCK) {
            return getGame().getRule().getInteger(UNIT_CAPACITY);
        }
    }

    public boolean isReady() {
        int player_count = 0;
        int alliance = -1;
        boolean alliance_ready = false;
        for (int team = 0; team < 4; team++) {
            if (getMap().hasTeamAccess(team) && allocation[team] != -1) {
                player_count++;
                if (alliance == -1) {
                    alliance = getAlliance(team);
                } else {
                    if (alliance != getAlliance(team)) {
                        alliance_ready = true;
                    }
                }
            }
        }
        return player_count >= 2 && alliance_ready;
    }

    public boolean isGameOver() {
        return getGame().isGameOver();
    }

    public void startGame() {
        for (int team = 0; team < 4; team++) {
            Player player = getGame().getPlayer(team);
            if (player.getType() != Player.NONE) {
                player.setType(Player.REMOTE);
            }
        }
        if (!getGame().initialized()) {
            getGame().initialize();
            getGame().gainIncome(getGame().getCurrentTeam());
        }
        game_started = true;
    }

    public void submitGameEvemt(Array<JSONObject> events, int player_id) {
        for (JSONObject event : events) {
            event_executor.submit(new GameEventExecuteTask(event, player_id));
        }
    }

    public void dispose() {
        event_executor.shutdown();
    }

    public boolean isOpen() {
        return !game_started;
    }

    public RoomSnapshot createSnapshot() {
        RoomSnapshot snapshot = new RoomSnapshot();
        snapshot.room_number = getRoomID();
        snapshot.open = isOpen();
        snapshot.requires_password = password != null;
        snapshot.room_name = getRoomName();
        snapshot.map_name = getMapName();
        snapshot.capacity = getPlayerCapacity();
        snapshot.remaining = getRemaining();
        return snapshot;
    }

    private class GameEventExecuteTask implements Runnable {

        private final JSONObject event;
        private final int player_id;

        public GameEventExecuteTask(JSONObject event, int player_id) {
            this.event = event;
            this.player_id = player_id;
        }

        @Override
        public void run() {
            try {
                synchronized (GAME_LOCK) {
                    getManager().getGameEventExecutor().submitGameEvent(event);
                    getManager().getGameEventExecutor().dispatchGameEvents();
                    getListener().onGameEventExecuted(Room.this, event, player_id);
                }
            } catch (CheatingException ex) {
                getListener().onCheatingDetected(Room.this, player_id, ex);
            }
        }

    }

}
