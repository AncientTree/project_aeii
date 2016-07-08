package net.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.utils.UnitFactory;

/**
 * @author toyknight 6/12/2016.
 */
public class UnitFrame extends AEIIWidget {

    private int index;

    public UnitFrame(GameContext context) {
        super(context);
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
        batch.draw(getResources().getBorderLightColor(), x, y, width, height);
        batch.draw(getResources().getListBackground(), x + ts / 12, y + ts / 12, width - ts / 6, height - ts / 6);
        getContext().getCanvasRenderer().drawUnit_(batch, UnitFactory.getSample(index), x + ts / 2, y + ts / 2, 0, ts);
        batch.flush();
    }

}
