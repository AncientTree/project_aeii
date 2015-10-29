package com.toyknight.aeii.serializable;

import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.entity.GameCore;

import java.io.Serializable;

/**
 * @author toyknight 8/31/2015.
 */
public class RoomConfiguration implements Serializable {

    private static final long serialVersionUID = 8312015L;

    public long room_number;

    public int host;

    public boolean started;

    public Array<PlayerSnapshot> players;

    public GameCore game;

    public Integer[] team_allocation;

    public int initial_gold;

    public int max_population;

}
