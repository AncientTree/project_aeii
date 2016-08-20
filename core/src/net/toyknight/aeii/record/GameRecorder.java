package net.toyknight.aeii.record;

import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.concurrent.RecordSaveTask;
import net.toyknight.aeii.entity.GameCore;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 9/22/2015.
 */
public class GameRecorder {

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

    public void submitGameEvent(JSONObject event) {
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
