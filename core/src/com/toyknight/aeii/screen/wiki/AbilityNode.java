package com.toyknight.aeii.screen.wiki;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/14/2016.
 */
public class AbilityNode extends Tree.Node {

    private final int ability;

    public AbilityNode(int index, Actor actor) {
        super(actor);
        this.ability = index;
    }

    public int getAbility() {
        return ability;
    }

    public static AbilityNode create(int ability, Skin skin) {
        Label label = new Label(Language.getText("ABILITY_NAME_" + ability), skin);
        return new AbilityNode(ability, label);
    }

}
