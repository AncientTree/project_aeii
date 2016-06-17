package com.toyknight.aeii.screen.wiki;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/16/2016.
 */
public class EntryNode extends Tree.Node {

    public static final int TYPE_ABILITY = 0x1;
    public static final int TYPE_STATUS = 0x2;
    public static final int TYPE_TILE = 0x3;
    public static final int TYPE_UNIT = 0x4;

    private final int type;
    private final int value;

    public EntryNode(int type, int value, Actor actor) {
        super(actor);
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public static EntryNode create(int type, int ability, Skin skin) {
        Label label;
        switch (type) {
            case TYPE_ABILITY:
                label = new Label(Language.getAbilityName(ability), skin);
                break;
            case TYPE_STATUS:
                label = new Label(Language.getStatusName(ability), skin);
                break;
            case TYPE_TILE:
                label = new Label("", skin);
                break;
            case TYPE_UNIT:
                label = new Label(Language.getUnitName(ability), skin);
                break;
            default:
                label = new Label("", skin);
        }
        return new EntryNode(type, ability, label);
    }

}
