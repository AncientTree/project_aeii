package com.toyknight.aeii.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.esotericsoftware.kryo.Kryo;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.manager.GameEvent;
import com.toyknight.aeii.net.Notification;
import com.toyknight.aeii.net.Request;
import com.toyknight.aeii.net.Response;
import com.toyknight.aeii.rule.Rule;
import com.toyknight.aeii.serializable.PlayerSnapshot;
import com.toyknight.aeii.serializable.RoomConfiguration;
import com.toyknight.aeii.serializable.RoomSnapshot;

/**
 * @author toyknight 10/27/2015.
 */
public class ClassRegister {

    public void register(Kryo kryo) {
        kryo.register(Object[].class);
        kryo.register(Integer[].class);
        kryo.register(int[].class);
        kryo.register(short[].class);
        kryo.register(short[][].class);
        kryo.register(boolean[].class);

        kryo.register(Request.class);
        kryo.register(Response.class);
        kryo.register(Notification.class);

        kryo.register(Array.class);
        kryo.register(Array.ArrayIterable.class);
        kryo.register(Array.ArrayIterator.class);
        kryo.register(ObjectSet.class);
        kryo.register(ObjectMap.class);
        kryo.register(ObjectMap.Keys.class);
        kryo.register(ObjectMap.Values.class);

        kryo.register(Point.class);
        kryo.register(Point[].class);
        kryo.register(Point[][].class);

        kryo.register(RoomSnapshot.class);
        kryo.register(PlayerSnapshot.class);
        kryo.register(RoomConfiguration.class);

        kryo.register(Statistics.class);
        kryo.register(GameCore.class);
        kryo.register(Player.class);
        kryo.register(Player[].class);
        kryo.register(Unit.class);
        kryo.register(Unit[].class);
        kryo.register(Unit[][].class);
        kryo.register(Rule.class);
        kryo.register(Map.class);
        kryo.register(Tomb.class);
        kryo.register(Status.class);
        kryo.register(GameEvent.class);
    }

}
