package com.toyknight.aeii.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.toyknight.aeii.GameManager;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.player.LocalPlayer;
import com.toyknight.aeii.screen.GameScreen;

/**
 * Created by toyknight on 4/19/2015.
 */
public class StatusBarRenderer {

    private final int ts;
    private final int hud_size;
    private final int margin_left;
    private final int margin_bottom;
    private final int max_pop_width;
    private final GameScreen screen;

    public StatusBarRenderer(GameScreen screen, int ts) {
        this.ts = ts;
        this.hud_size = ts / 24 * 11;
        this.max_pop_width = FontRenderer.getLNumberWidth(9999, false);
        int max_gold_width = FontRenderer.getLFractionWidth(99, 99);
        this.margin_left = (screen.getViewportWidth() - ts - max_pop_width - max_gold_width - hud_size * 2) / 3;
        this.margin_bottom = (ts - hud_size) / 2;
        this.screen = screen;
    }

    public void drawStatusBar(SpriteBatch batch, GameManager manager) {
        batch.begin();
        int current_team = manager.getGame().getCurrentTeam();
        batch.draw(ResourceManager.getTeamBackground(current_team),
                0, 0, Gdx.app.getGraphics().getWidth() - screen.getRightPanelWidth(), ts);
        drawSelectedTile(batch, manager);
        drawInformation(batch, manager);
        BorderRenderer.drawBorder(batch, 0, 0, ts, ts);
        BorderRenderer.drawBorder(batch, ts, 0, Gdx.app.getGraphics().getWidth() - screen.getRightPanelWidth() - ts, ts);
        batch.end();
    }

    private void drawInformation(SpriteBatch batch, GameManager manager) {
        int gold = manager.getGame().getCurrentPlayer().getGold();
        int current_pop = manager.getGame().getCurrentPlayer().getPopulation();
        int max_pop = manager.getGame().getRule().getMaxPopulation();
        //draw population
        batch.draw(ResourceManager.getStatusHudIcon(0), ts + margin_left, margin_bottom, hud_size, hud_size);
        FontRenderer.drawLFraction(batch, current_pop, max_pop, ts + margin_left + hud_size, margin_bottom);
        //draw gold
        batch.draw(ResourceManager.getStatusHudIcon(1), ts + margin_left * 2 + hud_size + max_pop_width, margin_bottom, hud_size, hud_size);
        if (manager.getGame().getCurrentPlayer() instanceof LocalPlayer) {
            FontRenderer.drawLNumber(batch, gold, ts + margin_left * 2 + hud_size * 2 + max_pop_width, margin_bottom);
        } else {
            batch.draw(FontRenderer.getLMinus(), ts + margin_left * 2 + hud_size * 2 + max_pop_width, margin_bottom);
        }
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
