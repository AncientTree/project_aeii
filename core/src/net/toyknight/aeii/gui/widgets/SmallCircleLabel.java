package net.toyknight.aeii.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/12/2016.
 */
public class SmallCircleLabel extends Widget {

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

    public SmallCircleLabel(TextureRegion icon) {
        int ts = AER.ts;
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
        batch.draw(AER.resources.getTextBackground(), x + tfp, y + tfp, width - tfp, tfh);
        batch.draw(AER.resources.getSmallCircleTexture(0), x, y, scw, sch);
        batch.draw(icon, x + ipx, y + ipy, iw, ih);
        AER.font.setTextColor(color_text);
        AER.font.drawText(batch, text,
                x + scw + AER.ts / 12, y + height - (height - AER.resources.getTextFont().getCapHeight()) / 2);
        AER.font.setTextColor(Color.WHITE);
    }

}
