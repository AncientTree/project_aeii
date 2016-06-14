package com.toyknight.aeii.screen.wiki;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/12/2016.
 */
public class AbilityLabel extends LabelButton {

    private final int ability;

    public AbilityLabel(int ability, Skin skin) {
        super("", skin);
        this.ability = ability;
        this.setText(String.format("[%s]", Language.getText("ABILITY_NAME_" + ability)));
    }

    public int getAbility() {
        return ability;
    }

}
