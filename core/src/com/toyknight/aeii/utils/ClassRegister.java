package com.toyknight.aeii.utils;

import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.net.Request;
import com.toyknight.aeii.net.Response;
import com.toyknight.aeii.serializable.RoomSnapshot;

/**
 * @author toyknight 10/27/2015.
 */
public class ClassRegister {

    public void register(Kryo kryo) {
        kryo.register(Request.class);
        kryo.register(Response.class);
        kryo.register(Object[].class);
        kryo.register(Array.class);
        kryo.register(RoomSnapshot.class);
        kryo.register(Map.class);
    }

}
