package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.toyknight.aeii.screen.LobbyScreen;

/**
 * Created by toyknight on 9/17/2015.
 */
public class RoomCreateLoadGameDialog extends BasicDialog {

    public RoomCreateLoadGameDialog(LobbyScreen lobby_screen) {
        super(lobby_screen);
        int width = ts * 11;
        this.setBounds((Gdx.graphics.getWidth() - width) / 2, ts / 2, width, Gdx.graphics.getHeight() - ts);
    }

    

    @Override
    public LobbyScreen getOwner() {
        return (LobbyScreen) super.getOwner();
    }

}
