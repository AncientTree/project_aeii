package com.toyknight.aeii.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.LocalGameManager;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.screen.GameScreen;

/**
 * Created by toyknight on 4/26/2015.
 */
public class RightPanelRenderer {

    private final int ts;
    private final GameScreen screen;

    public RightPanelRenderer(GameScreen screen, int ts) {
        this.ts = ts;
        this.screen = screen;
    }

    public void drawStatusBar(SpriteBatch batch, LocalGameManager manager) {
        batch.draw(ResourceManager.getPanelBackground(),
                Gdx.graphics.getWidth() - screen.getRightPanelWidth(), 0,
                screen.getRightPanelWidth(), Gdx.graphics.getHeight());
        BorderRenderer.drawBorder(batch,
                Gdx.graphics.getWidth() - screen.getRightPanelWidth(), ts,
                screen.getRightPanelWidth(), Gdx.graphics.getHeight() - ts * 2);
        batch.flush();
    }

}
