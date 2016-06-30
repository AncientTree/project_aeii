package net.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.renderer.UnitRenderer;
import net.toyknight.aeii.utils.UnitFactory;

/**
 * @author toyknight 6/12/2016.
 */
public class UnitFrame extends Widget {

    private final int ts;

    private int index;

    public UnitFrame(int ts) {
        this.ts = ts;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public float getPrefWidth() {
        return ts * 2;
    }

    @Override
    public float getPrefHeight() {
        return ts * 2;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        batch.draw(ResourceManager.getBorderLightColor(), x, y, width, height);
        batch.draw(ResourceManager.getListBackground(), x + ts / 12, y + ts / 12, width - ts / 6, height - ts / 6);
        UnitRenderer.drawUnit_(batch, UnitFactory.getSample(index), x + ts / 2, y + ts / 2, 0, ts);
        batch.flush();
    }

}
