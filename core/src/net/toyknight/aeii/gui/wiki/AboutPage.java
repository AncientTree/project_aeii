package net.toyknight.aeii.gui.wiki;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.gui.widgets.AEIITable;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/17/2016.
 */
public class AboutPage extends AEIITable {

    private final Wiki wiki;

    public AboutPage(Wiki wiki) {
        super(wiki.getContext());
        this.wiki = wiki;

        Label label_about_p1 = new Label(AER.lang.getText("WIKI_ABOUT_P1"), getWiki().getContext().getSkin());
        label_about_p1.setWrap(true);
        add(label_about_p1).width(ts * 7).padBottom(ts / 8).row();
        Label label_about_p2 = new Label(AER.lang.getText("WIKI_ABOUT_P2"), getWiki().getContext().getSkin());
        label_about_p2.setWrap(true);
        add(label_about_p2).width(ts * 7).padBottom(ts / 8).row();

        Label label_credits = new Label(AER.lang.getText("LB_CREDITS"), getWiki().getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };
        label_credits.setAlignment(Align.center);
        add(label_credits).size(ts * 7, label_credits.getPrefHeight() + ts / 4).row();

        Label label_program = new Label("[" + AER.lang.getText("LB_PROGRAM") + "]", getWiki().getContext().getSkin());
        label_program.setColor(90 / 256f, 150 / 256f, 77 / 256f, 1.0f);
        add(label_program).width(ts * 7).padBottom(ts / 8).padTop(ts / 8).row();

        addName("toyknight");
        addName("Majirefy");
        addName("blankwave");

        Label label_arts = new Label("[" + AER.lang.getText("LB_ARTS") + "]", getWiki().getContext().getSkin());
        label_arts.setColor(90 / 256f, 150 / 256f, 77 / 256f, 1.0f);
        add(label_arts).width(ts * 7).padBottom(ts / 8).padTop(ts / 8).row();

        addName("Ocean");
        addName("gr0wlithee");

        Label label_balancing = new Label("[" + AER.lang.getText("LB_BALANCING") + "]", getWiki().getContext().getSkin());
        label_balancing.setColor(90 / 256f, 150 / 256f, 77 / 256f, 1.0f);
        add(label_balancing).width(ts * 7).padBottom(ts / 8).padTop(ts / 8).row();

        addName("blankwave");
        addName("youxing");
        addName("td209");
        addName("Luke");
        addName("lingyun");

        Label label_translation = new Label("[" + AER.lang.getText("LB_TRANSLATION") + "]", getWiki().getContext().getSkin());
        label_translation.setColor(90 / 256f, 150 / 256f, 77 / 256f, 1.0f);
        add(label_translation).width(ts * 7).padBottom(ts / 8).padTop(ts / 8).row();

        addName("AncientTree (Chinese)");
        addName("P. Thanh Huy (Vietnamese)");
        addName("Dandandandaann (Portuguese)");
        addName("ortegafoust (Russian)");
    }

    private void addName(String name) {
        add(new Label(name, getWiki().getContext().getSkin())).width(ts * 7).padBottom(ts / 8).row();
    }

    public Wiki getWiki() {
        return wiki;
    }

}
