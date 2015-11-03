package com.toyknight.aeii.record;

import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.manager.GameEvent;
import com.toyknight.aeii.screen.GameScreen;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 10/30/2015.
 */
public class GameRecordPlayer {

    private final GameScreen screen;

    private GameRecord record;
    private float playback_delay;
    private boolean playback_finished;

    public GameRecordPlayer(GameScreen screen) {
        this.screen = screen;
    }

    public GameScreen getScreen() {
        return screen;
    }

    public GameManager getManager() {
        return getScreen().getManager();
    }

    public GameRecord getRecord() {
        return record;
    }

    public void setRecord(GameRecord record) {
        this.record = record;
        playback_delay = 0f;
    }

    public void update(float delta) {
        if (getRecord() != null) {
            if (getRecord().getEvents().isEmpty()) {
                if (!playback_finished) {
                    playback_finished = true;
                    getScreen().appendMessage(null, Language.getText("MSG_INFO_RPF"));
                }
            } else {
                GameEvent preview = getRecord().getEvents().peek();
                if (preview.getType() == GameEvent.TILE_DESTROY || preview.getType() == GameEvent.ATTACK) {
                    GameEvent event = getRecord().getEvents().poll();
                    getManager().submitGameEvent(event);
                } else {
                    if (playback_delay < 1.0f) {
                        playback_delay += delta;
                    } else {
                        playback_delay = 0f;
                        GameEvent event = getRecord().getEvents().poll();
                        getManager().submitGameEvent(event);
                    }
                }
            }
        }
    }

}
