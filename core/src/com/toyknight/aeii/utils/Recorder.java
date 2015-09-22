package com.toyknight.aeii.utils;

import com.badlogic.gdx.files.FileHandle;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.manager.events.GameEvent;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 9/22/2015.
 */
public class Recorder {

    private final static Queue<GameEvent> event_queue = new LinkedList<GameEvent>();

    private static boolean record_on;

    private static FileHandle record_file;

    private static ObjectOutputStream oos;

    public static void setRecord(boolean on) {
        Recorder.record_on = on;
        event_queue.clear();
        if (record_on) {
            String filename = GameFactory.createFilename(GameFactory.RECORD);
            record_file = FileProvider.getUserFile("save/" + filename);
        }
    }

    public static void prepare(GameCore game) throws IOException {
        if (record_on) {
            oos = new ObjectOutputStream(record_file.write(false));
            oos.writeInt(GameFactory.RECORD);
            oos.writeObject(game);
        }
    }

    public static void submitGameEvent(GameEvent event) {
        if (record_on) {
            event_queue.add(event);
        }
    }

    public static void saveRecord() throws IOException {
        if (record_on) {
            oos.writeObject(event_queue);
            oos.close();
        }
    }

}
