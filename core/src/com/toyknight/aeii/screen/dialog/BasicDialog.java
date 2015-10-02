package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.toyknight.aeii.AEIIApplet;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.screen.StageScreen;

/**
 * @author toyknight 9/17/2015.
 */
public class BasicDialog extends Table {

    protected final int ts;
    private final StageScreen owner;

    public BasicDialog(StageScreen owner) {
        this.owner = owner;
        this.ts = getContext().getTileSize();
    }

    public AEIIApplet getContext() {
        return getOwner().getContext();
    }

    public StageScreen getOwner() {
        return owner;
    }

    public void display() {
    }

    protected void drawCustom(Batch batch, float parentAlpha) {
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(ResourceManager.getPanelBackground(), getX(), getY(), getWidth(), getHeight());
        BorderRenderer.drawBorder(batch, getX(), getY(), getWidth(), getHeight());
        batch.flush();
        drawCustom(batch, parentAlpha);
        super.draw(batch, parentAlpha);
    }

}
