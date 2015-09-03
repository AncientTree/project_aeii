package com.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Created by toyknight on 9/2/2015.
 */
public class PlayerAllocationButton extends TextButton {

    private int team;

    public PlayerAllocationButton(int team, Skin skin) {
        super("=>", skin);
        this.team = team;
    }

    public int getTeam() {
        return team;
    }

}
