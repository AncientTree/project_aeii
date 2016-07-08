package net.toyknight.aeii.screen.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.entity.Status;
import net.toyknight.aeii.utils.UnitFactory;

/**
 * @author toyknight 6/17/2016.
 */
public class PreviewFrame extends AEIIWidget {

    private int tile_index;
    private int unit_index;

    private int status;

    public PreviewFrame(GameContext context) {
        this(context, 0, 0, -1);
    }

    public PreviewFrame(GameContext context, int tile_index, int unit_index, int status) {
        super(context);
        this.tile_index = tile_index;
        this.unit_index = unit_index;
        this.status = status;
    }

    public void setTileIndex(int tile_index) {
        this.tile_index = tile_index;
    }

    public void setUnitIndex(int unit_index) {
        this.unit_index = unit_index;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public float getPrefWidth() {
        return ts;
    }

    @Override
    public float getPrefHeight() {
        return ts;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        batch.draw(getResources().getTileTexture(tile_index), x, y, width, height);
        getContext().getBorderRenderer().drawBorder(batch, x, y, width, height);
        if (unit_index >= 0) {
            getContext().getCanvasRenderer().drawUnit_(batch, UnitFactory.getSample(unit_index), x, y, 0, ts);
        }

        int sw = ts * getResources().getStatusTexture(0).getRegionWidth() / 24;
        int sh = ts * getResources().getStatusTexture(0).getRegionHeight() / 24;
        switch (status) {
            case Status.BLINDED:
                batch.draw(getResources().getStatusTexture(3), x, y + height - sh, sw, sh);
                break;
            case Status.INSPIRED:
                batch.draw(getResources().getStatusTexture(1), x, y + height - sh, sw, sh);
                break;
            case Status.POISONED:
                batch.draw(getResources().getStatusTexture(0), x, y + height - sh, sw, sh);
                break;
            case Status.SLOWED:
                batch.draw(getResources().getStatusTexture(2), x, y + height - sh, sw, sh);
                break;
        }
        batch.flush();
    }

}
