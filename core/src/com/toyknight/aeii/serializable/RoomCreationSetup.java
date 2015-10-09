package com.toyknight.aeii.serializable;

import com.toyknight.aeii.entity.Map;

import java.io.Serializable;

/**
 * @author toyknight 10/8/2015.
 */
public class RoomCreationSetup implements Serializable {

    private static final long serialVersionUID = 10082015L;

    public String map_name;

    public Map map;

    public int capacity;

    public int initial_gold;

    public int population;

}
