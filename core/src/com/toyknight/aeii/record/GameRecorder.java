package com.toyknight.aeii.record;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.concurrent.RecordSaveTask;
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

    private final GameContext context;

    private Queue<JSONObject> event_queue = new LinkedList<JSONObject>();

    private boolean enabled = false;

    private GameRecord record;

    public GameRecorder(GameContext context) {
        this.context = context;
    }

    public GameContext getContext() {
        return context;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        event_queue.clear();
        record = null;
    }

    public void prepare(GameCore game) {
        if (enabled) {
            String V_STRING = getContext().getVerificationString();
            record = new GameRecord(V_STRING);
            record.setGame(new GameCore(game));
        }
    }

    public void submit(JSONObject event) {
        if (enabled) {
            event_queue.add(event);
        }
    }

    public void save() {
        if (enabled) {
            record.setEvents(event_queue);
            RecordSaveTask task = new RecordSaveTask(record);
            getContext().submitAsyncTask(task);
        }
    }

}
