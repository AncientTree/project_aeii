package com.toyknight.aeii.record;

import com.badlogic.gdx.Gdx;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.manager.GameEvent;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author toyknight 10/30/2015.
 */
public class GameRecordPlayer {

    private static final String TAG = "Record Player";

    private final GameContext context;

    private GameRecordPlayerListener listener;

    private GameRecord record;
    private float playback_delay;
    private boolean playback_finished;

    public GameRecordPlayer(GameContext context) {
        this.context = context;
    }

    public GameContext getContext() {
        return context;
    }

    public GameManager getManager() {
        return getContext().getGameManager();
    }

    public GameRecord getRecord() {
        return record;
    }

    public void setListener(GameRecordPlayerListener listener) {
        this.listener = listener;
    }

    public void setRecord(GameRecord record) {
        this.record = record;
        playback_delay = 0f;
        playback_finished = false;
    }

    public void reset() {
        record = null;
    }

    public void update(float delta) {
        try {
            if (getRecord() != null) {
                if (getRecord().getEvents().isEmpty()) {
                    if (!playback_finished) {
                        playback_finished = true;
                        fireRecordFinishEvent();
                    }
                } else {
                    JSONObject preview = getRecord().getEvents().peek();
                    int type = preview.getInt("type");
                    if (type == GameEvent.TILE_DESTROY || type == GameEvent.ATTACK) {
                        JSONObject event = getRecord().getEvents().poll();
                        getManager().getGameEventExecutor().submitGameEvent(event);
                    } else {
                        if (playback_delay < 1.0f) {
                            playback_delay += delta;
                        } else {
                            playback_delay = 0f;
                            JSONObject event = getRecord().getEvents().poll();
                            getManager().getGameEventExecutor().submitGameEvent(event);
                        }
                    }
                }
            }
        } catch (JSONException ex) {
            Gdx.app.log(TAG, ex.toString());
        }
    }

    private void fireRecordFinishEvent() {
        if (listener != null) {
            listener.onRecordPlaybackFinished();
        }
    }

}
