package net.toyknight.aeii.gui.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import net.toyknight.aeii.entity.Status;
import net.toyknight.aeii.renderer.BorderRenderer;
import net.toyknight.aeii.renderer.CanvasRenderer;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/17/2016.
 */
public class PreviewFrame extends Widget {

    private int tile_index;
    private int unit_index;

    private int status;

    public PreviewFrame() {
        this(0, 0, -1);
    }

    public PreviewFrame(int tile_index, int unit_index, int status) {
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
        return AER.ts;
    }

    @Override
    public float getPrefHeight() {
        return AER.ts;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        int ts = AER.ts;
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        batch.draw(AER.resources.getTileTexture(tile_index), x, y, width, height);
        BorderRenderer.drawBorder(batch, x, y, width, height);
        if (unit_index >= 0) {
            CanvasRenderer.drawUnit_(batch, AER.units.getSample(unit_index), x, y, 0, ts);
        }

        int sw = ts * AER.resources.getStatusTexture(0).getRegionWidth() / 24;
        int sh = ts * AER.resources.getStatusTexture(0).getRegionHeight() / 24;
        switch (status) {
            case Status.BLINDED:
                batch.draw(AER.resources.getStatusTexture(3), x, y + height - sh, sw, sh);
                break;
            case Status.INSPIRED:
                batch.draw(AER.resources.getStatusTexture(1), x, y + height - sh, sw, sh);
                break;
            case Status.POISONED:
                batch.draw(AER.resources.getStatusTexture(0), x, y + height - sh, sw, sh);
                break;
            case Status.SLOWED:
                batch.draw(AER.resources.getStatusTexture(2), x, y + height - sh, sw, sh);
                break;
        }
        batch.flush();
    }

}
