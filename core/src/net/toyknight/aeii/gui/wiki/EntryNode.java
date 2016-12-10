package net.toyknight.aeii.gui.wiki;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.system.AER;
import net.toyknight.aeii.utils.TextureUtil;

/**
 * @author toyknight 6/16/2016.
 */
public class EntryNode extends Tree.Node {

    public static final int TYPE_ABILITY = 0x1;
    public static final int TYPE_STATUS = 0x2;
    public static final int TYPE_TILE = 0x3;
    public static final int TYPE_UNIT = 0x4;

    public static final int TYPE_GAMEPLAY_OBJECTIVES = 0x5;
    public static final int TYPE_GAMEPLAY_RECRUITING = 0x6;
    public static final int TYPE_GAMEPLAY_ATTACKING = 0x7;
    public static final int TYPE_GAMEPLAY_HEALING = 0x8;
    public static final int TYPE_GAMEPLAY_INCOME = 0x9;
    public static final int TYPE_GAMEPLAY_STATUS = 0x10;

    public static final int TYPE_MULTIPLAYER_CREATE_GAME = 0x11;
    public static final int TYPE_MULTIPLAYER_JOIN_GAME = 0x12;
    public static final int TYPE_MULTIPLAYER_COMMANDS = 0x13;

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

    public static EntryNode create(int type, int ts, GameContext context) {
        return create(type, 0, ts, context);
    }

    public static EntryNode create(int type, int value, int ts, GameContext context) {
        Label label;
        switch (type) {
            case TYPE_ABILITY:
                label = new Label(AER.lang.getAbilityName(value), context.getSkin());
                break;
            case TYPE_STATUS:
                label = new Label(AER.lang.getStatusName(value), context.getSkin());
                break;
            case TYPE_TILE:
                Table tile_label = new Table();
                TextureRegionDrawable drawable_tile =
                        TextureUtil.createDrawable(AER.resources.getTileTexture(value), ts, ts);
                Image image_tile = new Image(drawable_tile);
                tile_label.add(image_tile);
                Label tile_name = new Label(AER.lang.getText("LB_TILE_NUMBER") + value, context.getSkin());
                tile_label.add(tile_name).padLeft(ts / 8);
                return new EntryNode(type, value, tile_label);
            case TYPE_UNIT:
                label = new Label(AER.lang.getUnitName(value), context.getSkin());
                break;
            case EntryNode.TYPE_GAMEPLAY_ATTACKING:
                label = new Label(AER.lang.getText("WIKI_NODE_GAMEPLAY_ATTACKING"), context.getSkin());
                break;
            case EntryNode.TYPE_GAMEPLAY_HEALING:
                label = new Label(AER.lang.getText("WIKI_NODE_GAMEPLAY_HEALING"), context.getSkin());
                break;
            case EntryNode.TYPE_GAMEPLAY_INCOME:
                label = new Label(AER.lang.getText("WIKI_NODE_GAMEPLAY_INCOME"), context.getSkin());
                break;
            case EntryNode.TYPE_GAMEPLAY_OBJECTIVES:
                label = new Label(AER.lang.getText("WIKI_NODE_GAMEPLAY_OBJECTIVES"), context.getSkin());
                break;
            case EntryNode.TYPE_GAMEPLAY_RECRUITING:
                label = new Label(AER.lang.getText("WIKI_NODE_GAMEPLAY_RECRUITING"), context.getSkin());
                break;
            case EntryNode.TYPE_GAMEPLAY_STATUS:
                label = new Label(AER.lang.getText("WIKI_NODE_GAMEPLAY_STATUS"), context.getSkin());
                break;
            case TYPE_MULTIPLAYER_COMMANDS:
                label = new Label(AER.lang.getText("WIKI_NODE_MULTIPLAYER_COMMANDS"), context.getSkin());
                break;
            case TYPE_MULTIPLAYER_CREATE_GAME:
                label = new Label(AER.lang.getText("WIKI_NODE_MULTIPLAYER_CREATE_GAME"), context.getSkin());
                break;
            case TYPE_MULTIPLAYER_JOIN_GAME:
                label = new Label(AER.lang.getText("WIKI_NODE_MULTIPLAYER_JOIN_GAME"), context.getSkin());
                break;
            default:
                label = new Label("", context.getSkin());
        }
        return new EntryNode(type, value, label);
    }

}
