package com.toyknight.aeii.network.server;

import static com.toyknight.aeii.entity.Rule.Entry.*;

import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.network.entity.RoomSnapshot;
import com.toyknight.aeii.entity.Rule;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * @author toyknight
 */
public class Room {

    public final Object GAME_LOCK = new Object();

    private final Object PLAYER_LOCK = new Object();

    private final long room_number;
    private final String room_name;

    private boolean game_started;

    private int capacity = 4;

    private int host_player_id;
    private final ObjectSet<Integer> players;
    private final int[] allocation;

    private GameManager manager;

    private String map_name;
    private int start_gold = 1000;

    private String password = null;

    public Room(long room_number, String room_name, GameCore game) {
        this(room_number, room_name);
        manager = new GameManager();
        manager.setGame(game);
        start_gold = -1;
    }

    public Room(long room_number, String room_name) {
        this.room_number = room_number;
        this.room_name = room_name;
        this.game_started = false;
        this.players = new ObjectSet<Integer>();
        this.allocation = new int[4];
        Arrays.fill(allocation, -1);
        host_player_id = -1;
        game_started = false;
    }

    public void initialize(Map map) {
        synchronized (PLAYER_LOCK) {
            Rule rule = Rule.createDefault();
            GameCore game = new GameCore(map, rule, 0, GameCore.SKIRMISH);
            for (int team = 0; team < 4; team++) {
                game.getPlayer(team).setAlliance(team + 1);
            }
            manager = new GameManager();
            manager.setGame(game);
        }
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

    public long getRoomNumber() {
        return room_number;
    }

    public String getRoomName() {
        return room_name;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getRemaining() {
        return getCapacity() - players.size;
    }

    public void setMapName(String name) {
        this.map_name = name;
    }

    public String getMapName() {
        return map_name;
    }

    public void setStartGold(int gold) {
        synchronized (GAME_LOCK) {
            this.start_gold = gold;
            for (int team = 0; team < 4; team++) {
                Player player = getGame().getPlayer(team);
                player.setGold(gold);
            }
        }
    }

    public int getStartGold() {
        return start_gold;
    }

    public void setMaxPopulation(int population) {
        synchronized (GAME_LOCK) {
            getGame().getRule().setValue(MAX_POPULATION, population);
        }
    }

    public int getMaxPopulation() {
        synchronized (GAME_LOCK) {
            return getGame().getRule().getInteger(MAX_POPULATION);
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
        }
        game_started = true;
    }

    public void submitGameEvent(JSONObject event) {
        synchronized (GAME_LOCK) {
            getManager().getGameEventExecutor().submitGameEvent(event);
            while (!getGame().isGameOver() && getManager().getGameEventExecutor().isProcessing()) {
                getManager().getGameEventExecutor().dispatchGameEvents();
            }
        }
    }

    public boolean isOpen() {
        return !game_started;
    }

    public RoomSnapshot createSnapshot() {
        RoomSnapshot snapshot = new RoomSnapshot();
        snapshot.room_number = getRoomNumber();
        snapshot.open = isOpen();
        snapshot.requires_password = password != null;
        snapshot.room_name = getRoomName();
        snapshot.map_name = getMapName();
        snapshot.capacity = getCapacity();
        snapshot.remaining = getRemaining();
        return snapshot;
    }

}
