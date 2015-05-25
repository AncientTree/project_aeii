package com.toyknight.aeii.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.GameManager;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.screen.GameScreen;

/**
 * Created by toyknight on 4/19/2015.
 */
public class StatusBarRenderer {

    private final int ts;
    private final GameScreen screen;

    public StatusBarRenderer(GameScreen screen, int ts) {
        this.ts = ts;
        this.screen = screen;
    }

    public void drawStatusBar(SpriteBatch batch, GameManager manager) {
        batch.begin();
        int current_team = manager.getGame().getCurrentTeam();
        batch.draw(ResourceManager.getTeamBackground(current_team),
                0, 0, Gdx.app.getGraphics().getWidth() - screen.getRightPanelWidth(), ts);
        drawSelectedTile(batch, manager);
        BorderRenderer.drawBorder(batch, 0, 0, ts, ts);
        BorderRenderer.drawBorder(batch, ts, 0, Gdx.app.getGraphics().getWidth() - screen.getRightPanelWidth() - ts, ts);
        batch.end();
    }

    private void drawSelectedTile(SpriteBatch batch, GameManager manager) {
        int cursor_x = screen.getCursorMapX();
        int cursor_y = screen.getCursorMapY();
        if (cursor_x >= 0 && cursor_y >= 0) {
            short tile_index = manager.getGame().getMap().getTileIndex(cursor_x, cursor_y);
            batch.draw(ResourceManager.getTileTexture(tile_index), 0, 0, ts, ts);
            //draw defence bonus
            FontRenderer.drawTileDefenceBonus(
                    batch, manager.getGame().getMap().getTile(cursor_x, cursor_y).getDefenceBonus(),
                    (ts - FontRenderer.getSNumberWidth(99, true)) / 2, ts / 12);
        }
    }

}
