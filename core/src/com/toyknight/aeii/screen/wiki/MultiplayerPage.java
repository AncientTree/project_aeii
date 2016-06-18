package com.toyknight.aeii.screen.wiki;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/18/2016.
 */
public class MultiplayerPage extends Table {

    private final int ts;

    private final Wiki wiki;

    private final Label label_command_assign;

    public MultiplayerPage(Wiki wiki) {
        super();
        this.wiki = wiki;
        this.ts = getWiki().getContext().getTileSize();

        label_command_assign = new Label("[/assign <team>]", getWiki().getContext().getSkin());
        label_command_assign.setColor(Color.LIME);
    }

    public Wiki getWiki() {
        return wiki;
    }

    private void addParagraph(String text) {
        Label label_text = new Label(text, getWiki().getContext().getSkin());
        label_text.setWrap(true);
        add(label_text).width(ts * 7).padBottom(ts / 8).row();
    }

    public void setType(int type) {
        clearChildren();
        switch (type) {
            case EntryNode.TYPE_MULTIPLAYER_COMMANDS:
                add(label_command_assign).width(ts * 7).padBottom(ts / 8).row();
                addParagraph(Language.getText("WIKI_MULTIPLAYER_COMMANDS_ASSIGN_P1"));
                addParagraph(Language.getText("WIKI_MULTIPLAYER_COMMANDS_ASSIGN_P2"));
                break;
            case EntryNode.TYPE_MULTIPLAYER_CREATE_GAME:
                addParagraph(Language.getText("WIKI_MULTIPLAYER_CREATE_GAME_P1"));
                addParagraph(Language.getText("WIKI_MULTIPLAYER_CREATE_GAME_P2"));
                addParagraph(Language.getText("WIKI_MULTIPLAYER_CREATE_GAME_P3"));
                addParagraph(Language.getText("WIKI_MULTIPLAYER_CREATE_GAME_P4"));
                break;
            case EntryNode.TYPE_MULTIPLAYER_JOIN_GAME:
                addParagraph(Language.getText("WIKI_MULTIPLAYER_JOIN_GAME_P1"));
                addParagraph(Language.getText("WIKI_MULTIPLAYER_JOIN_GAME_P2"));
                break;
        }
    }

}
