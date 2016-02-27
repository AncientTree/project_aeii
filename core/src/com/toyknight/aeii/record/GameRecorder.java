package com.toyknight.aeii.record;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.utils.FileProvider;
import com.toyknight.aeii.utils.GameToolkit;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 9/22/2015.
 */
public class GameRecorder {

    private static final String TAG = "Recorder";

    private final static Queue<JSONObject> event_queue = new LinkedList<JSONObject>();

    private static boolean record_on;

    private static Output output;

    private static GameRecord record;

    public static void setRecord(boolean on) {
        GameRecorder.record_on = on;
        event_queue.clear();
        output = null;
        record = null;
    }

    public static void prepare(String V_STRING, GameCore game) {
        if (record_on) {
            try {
                String filename = GameToolkit.createFilename(GameToolkit.RECORD);
                FileHandle record_file = FileProvider.getUserFile("save/" + filename);
                output = new Output(record_file.write(false));
                output.writeInt(GameToolkit.RECORD);

                record = new GameRecord(V_STRING);
                record.setGame(new GameCore(game));
            } catch (KryoException ex) {
                GameRecorder.setRecord(false);
                Gdx.app.log(TAG, ex.toString());
            }
        }
    }

    public static void submitGameEvent(JSONObject event) {
        if (record_on) {
            Gdx.app.log(TAG, "Record " + event.toString());
            event_queue.add(event);
        }
    }

    public static void saveRecord() {
        if (record_on) {
            try {
                record.setEvents(event_queue);
                output.writeString(record.toJson().toString());
                output.flush();
                output.close();
            } catch (KryoException ex) {
                Gdx.app.log(TAG, ex.toString());
            }
        }
    }

}
