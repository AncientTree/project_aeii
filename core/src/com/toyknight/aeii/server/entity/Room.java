package com.toyknight.aeii.server.entity;

import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author toyknight
 */
public class Room {

    private final Object PLAYER_LOCK = new Object();

    private final long room_number;
    private final String room_name;

    private boolean game_started;

    private int capacity = 4;

    private String host_service = null;
    private final HashSet<String> players;
    private final int[] player_type;
    private final String[] team_allocation;
    private final int[] alliance_state;

    private Map map;
    private String map_name;
    private int initial_gold = 1000;
    private int max_population = 10;

    public Room(long room_number, String room_name) {
        this.room_number = room_number;
        this.room_name = room_name;
        this.game_started = false;
        this.players = new HashSet<String>();
        this.player_type = new int[4];
        this.team_allocation = new String[4];
        this.alliance_state = new int[4];
        reset();
    }

    public void reset() {
        host_service = null;
        players.clear();
        Arrays.fill(player_type, Player.NONE);
        Arrays.fill(team_allocation, "NONE");
        for (int team = 0; team < 4; team++) {
            alliance_state[team] = team + 1;
        }
        setGameStarted(false);
    }

    public boolean hasPlayer(String service_name) {
        return players.contains(service_name);
    }

    public Set<String> getPlayers() {
        synchronized (PLAYER_LOCK) {
            return new HashSet<String>(players);
        }
    }

    public void addPlayer(String service_name) {
        synchronized (PLAYER_LOCK) {
            players.add(service_name);
            if (isOpen()) {
                for (int team = 0; team < 4; team++) {
                    if (getMap().hasTeamAccess(team) && team_allocation[team].equals("NONE")) {
                        player_type[team] = Player.LOCAL;
                        team_allocation[team] = service_name;
                        break;
                    }
                }
            }
        }
    }

    public void removePlayer(String service_name) {
        synchronized (PLAYER_LOCK) {
            players.remove(service_name);
            if (isOpen()) {
                for (int team = 0; team < 4; team++) {
                    if (!team_allocation[team].equals("NONE") && team_allocation[team].equals(service_name)) {
                        player_type[team] = Player.NONE;
                        team_allocation[team] = "NONE";
                    }
                }
                if (host_service != null && host_service.equals(service_name)) {
                    host_service = null;
                }
            }
        }
    }

    public void setHostService(String service) {
        host_service = service;
    }

    public String getHostService() {
        return host_service;
    }

    public void setTeamAllocation(int team, String service) {
        team_allocation[team] = service;
    }

    public String[] getTeamAllocation() {
        return team_allocation;
    }

    public void setPlayerType(int team, int type) {
        player_type[team] = type;
    }

    public int getPlayerType(int team) {
        return player_type[team];
    }

    public Integer[] getPlayerType() {
        Integer[] types = new Integer[4];
        for (int i = 0; i < player_type.length; i++) {
            types[i] = player_type[i];
        }
        return types;
    }

    public void setAlliance(int team, int alliance) {
        alliance_state[team] = alliance;
    }

    public Integer[] getAllianceState() {
        Integer[] alliance = new Integer[4];
        for (int i = 0; i < alliance_state.length; i++) {
            alliance[i] = alliance_state[i];
        }
        return alliance;
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
        return getCapacity() - players.size();
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public Map getMap() {
        return map;
    }

    public void setMapName(String map_name) {
        this.map_name = map_name;
    }

    public String getMapName() {
        return map_name;
    }

    public void setInitialGold(int gold) {
        this.initial_gold = gold;
    }

    public int getInitialGold() {
        return initial_gold;
    }

    public void setMaxPopulation(int population) {
        this.max_population = population;
    }

    public int getMaxPopulation() {
        return max_population;
    }

    public boolean isReady() {
        int player_count = 0;
        int alliance = -1;
        boolean alliance_ready = false;
        for (int team = 0; team < 4; team++) {
            if (getMap().hasTeamAccess(team) && !team_allocation[team].equals("NONE")) {
                player_count++;
                if (alliance == -1) {
                    alliance = alliance_state[team];
                } else {
                    if (alliance != alliance_state[team]) {
                        alliance_ready = true;
                    }
                }
            }
        }
        return player_count >= 2 && alliance_ready;
    }

    public void setGameStarted(boolean started) {
        game_started = started;
    }

    public boolean isOpen() {
        return !game_started;
    }

    public RoomSnapshot createSnapshot() {
        return new RoomSnapshot(getRoomNumber(), isOpen(), getRoomName(), getMapName(), getCapacity(), getRemaining());
    }

}
