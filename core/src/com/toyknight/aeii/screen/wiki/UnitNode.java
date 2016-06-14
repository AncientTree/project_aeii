package com.toyknight.aeii.screen.wiki;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/12/2016.
 */
public class UnitNode extends Tree.Node {

    private final int index;

    public UnitNode(int index, Actor actor) {
        super(actor);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static UnitNode create(int index, Skin skin) {
        Label label = new Label(Language.getUnitName(index), skin);
        return new UnitNode(index, label);
    }

}
