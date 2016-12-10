package net.toyknight.aeii.gui.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.renderer.CanvasRenderer;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/12/2016.
 */
public class UnitFrame extends Widget {

    private int index;

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public float getPrefWidth() {
        return AER.ts * 2;
    }

    @Override
    public float getPrefHeight() {
        return AER.ts * 2;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        int ts = AER.ts;
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        batch.draw(AER.resources.getBorderLightColor(), x, y, width, height);
        batch.draw(AER.resources.getListBackground(), x + ts / 12, y + ts / 12, width - ts / 6, height - ts / 6);
        CanvasRenderer.drawUnit_(batch, AER.units.getSample(index), x + ts / 2, y + ts / 2, 0, ts);
        batch.flush();
    }

}
