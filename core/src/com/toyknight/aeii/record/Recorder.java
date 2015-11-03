package com.toyknight.aeii.record;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.manager.GameEvent;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.GameFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 9/22/2015.
 */
public class Recorder {

    private static final String TAG = "Recorder";

    private final static Queue<GameEvent> event_queue = new LinkedList<GameEvent>();

    private static boolean record_on;

    private static ObjectOutputStream oos;

    private static GameRecord record;

    public static void setRecord(boolean on) {
        Recorder.record_on = on;
        event_queue.clear();
        record = null;
    }

    public static void prepare(String V_STRING, GameCore game) {
        if (record_on) {
            try {
                String filename = GameFactory.createFilename(GameFactory.RECORD);
                FileHandle record_file = FileProvider.getUserFile("save/" + filename);
                oos = new ObjectOutputStream(record_file.write(false));
                oos.writeInt(GameFactory.RECORD);

                GameRecord record = new GameRecord(V_STRING);
                record.setGame(new GameCore(game));
            } catch (IOException ex) {
                Recorder.setRecord(false);
                Gdx.app.log("Record", ex.toString());
            }
        }
    }

    public static void submitGameEvent(GameEvent event) {
        if (record_on) {
            Gdx.app.log(TAG, "Record " + event.toString());
            event_queue.add(event);
        }
    }

    public static void saveRecord() throws IOException {
        if (record_on) {
            record.setEvents(event_queue);
            oos.writeObject(record);
            oos.close();
        }
    }

}
