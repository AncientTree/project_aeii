package net.toyknight.aeii.screen.wiki;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/12/2016.
 */
public class RootNode extends Tree.Node {

    public static final int OVERVIEW = 1;
    public static final int GAMEPLAY = 2;
    public static final int TERRAINS = 3;
    public static final int UNITS = 4;
    public static final int ABILITIES = 5;
    public static final int STATUS = 6;
    public static final int MULTIPLAYER = 7;
    public static final int ABOUT = 8;

    private final int type;

    public RootNode(int type, Actor actor) {
        super(actor);
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static RootNode create(int type, Skin skin) {
        Label label = new Label(Language.getText("WIKI_NODE_ROOT_" + type), skin);
        return new RootNode(type, label);
    }

}
