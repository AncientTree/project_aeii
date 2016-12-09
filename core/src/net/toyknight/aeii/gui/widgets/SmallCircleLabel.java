package net.toyknight.aeii.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.toyknight.aeii.GameContext;

/**
 * @author toyknight 6/12/2016.
 */
public class SmallCircleLabel extends AEIIWidget {

    private final TextureRegion icon;

    private final int scw;
    private final int sch;

    private final int iw;
    private final int ih;
    private final int ipx;
    private final int ipy;

    private final int tfh;
    private final int tfp;

    private String text;
    private Color color_text;

    public SmallCircleLabel(GameContext context, TextureRegion icon) {
        super(context);
        this.icon = icon;
        scw = ts * 20 / 24;
        sch = ts * 21 / 24;
        iw = ts * icon.getRegionWidth() / 24;
        ih = ts * icon.getRegionHeight() / 24;
        ipx = (scw - iw) / 2;
        ipy = (sch - ih) / 2;
        tfh = sch - ts / 4;
        tfp = (sch - tfh) / 2;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTextColor(Color color_text) {
        this.color_text = color_text;
    }

    @Override
    public float getPrefHeight() {
        return sch;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        batch.draw(getResources().getTextBackground(), x + tfp, y + tfp, width - tfp, tfh);
        batch.draw(getResources().getSmallCircleTexture(0), x, y, scw, sch);
        batch.draw(icon, x + ipx, y + ipy, iw, ih);
        getContext().getFontRenderer().setTextColor(color_text);
        getContext().getFontRenderer().drawText(batch, text,
                x + scw + ts / 12, y + height - (height - getResources().getTextFont().getCapHeight()) / 2);
        getContext().getFontRenderer().setTextColor(Color.WHITE);
    }

}
