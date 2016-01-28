package com.toyknight.aeii.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.esotericsoftware.kryo.Kryo;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.manager.GameEvent;
import com.toyknight.aeii.net.serializable.Notification;
import com.toyknight.aeii.net.serializable.Request;
import com.toyknight.aeii.net.serializable.Response;
import com.toyknight.aeii.record.GameRecord;
import com.toyknight.aeii.rule.Rule;
import com.toyknight.aeii.entity.GameSave;
import com.toyknight.aeii.net.serializable.PlayerSnapshot;
import com.toyknight.aeii.net.serializable.RoomSetting;
import com.toyknight.aeii.net.serializable.RoomSnapshot;

import java.util.LinkedList;

/**
 * @author toyknight 10/27/2015.
 */
public class ClassRegister {

    public void register(Kryo kryo) {
        kryo.register(Object.class);
        kryo.register(Object[].class);
        kryo.register(Integer.class);
        kryo.register(Integer[].class);
        kryo.register(Boolean.class);
        kryo.register(int[].class);
        kryo.register(short[].class);
        kryo.register(short[][].class);
        kryo.register(boolean[].class);
        kryo.register(LinkedList.class);

        kryo.register(Request.class);
        kryo.register(Response.class);
        kryo.register(Notification.class);

        kryo.register(Array.class);
        kryo.register(ObjectSet.class);
        kryo.register(ObjectMap.class);

        kryo.register(Position.class);
        kryo.register(Position[].class);
        kryo.register(Position[][].class);

        kryo.register(RoomSnapshot.class);
        kryo.register(PlayerSnapshot.class);
        kryo.register(RoomSetting.class);

        kryo.register(Statistics.class);
        kryo.register(GameCore.class);
        kryo.register(Player.class);
        kryo.register(Player[].class);
        kryo.register(Unit.class);
        kryo.register(Unit[].class);
        kryo.register(Unit[][].class);
        kryo.register(Rule.class);
        kryo.register(Rule.Entry.class);
        kryo.register(Map.class);
        kryo.register(Tomb.class);
        kryo.register(Status.class);
        kryo.register(GameSave.class);
        kryo.register(GameEvent.class);
        kryo.register(GameRecord.class);
    }

}
