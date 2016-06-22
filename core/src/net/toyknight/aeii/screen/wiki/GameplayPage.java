package net.toyknight.aeii.screen.wiki;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/17/2016.
 */
public class GameplayPage extends Table {

    private final int ts;

    private final Wiki wiki;

    public GameplayPage(Wiki wiki) {
        super();
        this.wiki = wiki;
        this.ts = getWiki().getContext().getTileSize();
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
            case EntryNode.TYPE_GAMEPLAY_ATTACKING:
                addParagraph(Language.getText("WIKI_GAMEPLAY_ATTACKING_P1"));
                addParagraph(Language.getText("WIKI_GAMEPLAY_ATTACKING_P2"));
                addParagraph(Language.getText("WIKI_GAMEPLAY_ATTACKING_P3"));
                addParagraph(Language.getText("WIKI_GAMEPLAY_ATTACKING_P4"));
                break;
            case EntryNode.TYPE_GAMEPLAY_HEALING:
                addParagraph(Language.getText("WIKI_GAMEPLAY_HEALING_P1"));
                addParagraph(Language.getText("WIKI_GAMEPLAY_HEALING_P2"));
                break;
            case EntryNode.TYPE_GAMEPLAY_INCOME:
                addParagraph(Language.getText("WIKI_GAMEPLAY_INCOME_P1"));
                addParagraph(Language.getText("WIKI_GAMEPLAY_INCOME_P2"));
                break;
            case EntryNode.TYPE_GAMEPLAY_OBJECTIVES:
                addParagraph(Language.getText("WIKI_GAMEPLAY_OBJECTIVES_P1"));
                addParagraph(Language.getText("WIKI_GAMEPLAY_OBJECTIVES_P2"));
                break;
            case EntryNode.TYPE_GAMEPLAY_RECRUITING:
                addParagraph(Language.getText("WIKI_GAMEPLAY_RECRUITING_P1"));
                addParagraph(Language.getText("WIKI_GAMEPLAY_RECRUITING_P2"));
                addParagraph(Language.getText("WIKI_GAMEPLAY_RECRUITING_P3"));
                break;
            case EntryNode.TYPE_GAMEPLAY_STATUS:
                addParagraph(Language.getText("WIKI_GAMEPLAY_STATUS_P1"));
                addParagraph(Language.getText("WIKI_GAMEPLAY_STATUS_P2"));
                break;
        }
    }

}
