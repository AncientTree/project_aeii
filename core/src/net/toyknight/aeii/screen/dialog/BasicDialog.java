package net.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.ResourceManager;
import net.toyknight.aeii.renderer.BorderRenderer;
import net.toyknight.aeii.screen.StageScreen;

/**
 * @author toyknight 9/17/2015.
 */
public class BasicDialog extends Table {

    protected final int ts;
    private StageScreen owner;

    private boolean top_bottom_border_enabled = false;

    public BasicDialog(StageScreen owner) {
        this.owner = owner;
        this.ts = getContext().getTileSize();
    }

    public GameContext getContext() {
        return getOwner().getContext();
    }

    public ResourceManager getResources() {
        return getContext().getResources();
    }

    public StageScreen getOwner() {
        return owner;
    }

    public void setTopBottomBorderEnabled(boolean top_bottom_border_enabled) {
        this.top_bottom_border_enabled = top_bottom_border_enabled;
    }

    public void setOwner(StageScreen owner) {
        this.owner = owner;
    }

    public void display() {
    }

    protected void drawCustom(Batch batch, float parentAlpha) {
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(getResources().getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        if (top_bottom_border_enabled) {
            getContext().getBorderRenderer().drawTopBottomBorder(batch, getX(), getY(), getWidth(), getHeight());
        } else {
            getContext().getBorderRenderer().drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        }
        batch.flush();
        drawCustom(batch, parentAlpha);
        super.draw(batch, parentAlpha);
    }

}
