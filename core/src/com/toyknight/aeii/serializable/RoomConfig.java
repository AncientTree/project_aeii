package com.toyknight.aeii.serializable;

import com.toyknight.aeii.entity.Map;

import java.io.Serializable;

/**
 * @author toyknight 8/31/2015.
 */
public class RoomConfig implements Serializable {

    private static final long serialVersionUID = 8312015L;

    public long room_number;

    public String host;

    public PlayerSnapshot[] players;

    public Map map;

    public Integer[] player_type;

    public String[] team_allocation;

    public Integer[] alliance_state;

    public int initial_gold;

    public int max_population;

}
