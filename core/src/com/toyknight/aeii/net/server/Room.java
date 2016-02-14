package com.toyknight.aeii.net.server;

import static com.toyknight.aeii.entity.Rule.Entry.*;

import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.manager.GameEvent;
import com.toyknight.aeii.net.serializable.RoomSnapshot;
import com.toyknight.aeii.entity.Rule;

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
    private final Integer[] team_allocation;

    private GameManager manager;

    private String map_name;
    private int start_gold = 1000;

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
        this.team_allocation = new Integer[4];
        Arrays.fill(team_allocation, -1);
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
                if (team_allocation[team] == id) {
                    setPlayerType(team, Player.NONE);
                    team_allocation[team] = -1;
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

    public Integer getHostID() {
        return host_player_id;
    }

    public void setTeamAllocation(int team, int id) {
        team_allocation[team] = id;
    }

    public Integer[] getTeamAllocation() {
        return team_allocation;
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

    public Integer[] getPlayerTypes() {
        synchronized (GAME_LOCK) {
            Integer[] player_types = new Integer[4];
            for (int team = 0; team < 4; team++) {
                Player player = getGame().getPlayer(team);
                player_types[team] = player.getType();
            }
            return player_types;
        }
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

    public Integer[] getAlliances() {
        Integer[] alliances = new Integer[4];
        for (int team = 0; team < 4; team++) {
            alliances[team] = getGame().getPlayer(team).getAlliance();
        }
        return alliances;
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
            if (getMap().hasTeamAccess(team) && team_allocation[team] != -1) {
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
        if (!getGame().isInitialized()) {
            getGame().initialize();
        }
        game_started = true;
    }

    public void submitGameEvent(GameEvent event) {
        synchronized (GAME_LOCK) {
            getManager().submitGameEvent(event);
            while (getManager().getGameEventExecutor().isProcessing()) {
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
        snapshot.room_name = getRoomName();
        snapshot.map_name = getMapName();
        snapshot.capacity = getCapacity();
        snapshot.remaining = getRemaining();
        return snapshot;
    }

}
