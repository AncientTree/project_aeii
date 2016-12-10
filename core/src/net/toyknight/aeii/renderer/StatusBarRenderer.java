package net.toyknight.aeii.renderer;

import static net.toyknight.aeii.entity.Rule.Entry.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.toyknight.aeii.manager.GameManager;
import net.toyknight.aeii.gui.GameScreen;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 4/19/2015.
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
        this.screen = screen;
        this.hud_size = ts / 24 * 11;
        this.max_pop_width = AER.font.getLNumberWidth(9999, false);
        int max_gold_width = AER.font.getLFractionWidth(99, 99);
        this.margin_left = (screen.getViewportWidth() - ts - max_pop_width - max_gold_width - hud_size * 2) / 3;
        this.margin_bottom = (ts - hud_size) / 2;
    }

    private GameManager getManager() {
        return screen.getGameManager();
    }

    public void drawStatusBar(SpriteBatch batch) {
        int current_team = getManager().getGame().getCurrentTeam();
        batch.draw(AER.resources.getTeamBackground(current_team),
                0, 0, Gdx.app.getGraphics().getWidth() - screen.getRightPanelWidth(), ts);
        batch.flush();
        drawSelectedTile(batch);
        drawInformation(batch);
        BorderRenderer.drawBorder(batch, 0, 0, ts, ts);
        BorderRenderer.drawBorder(
                batch, ts, 0, Gdx.app.getGraphics().getWidth() - screen.getRightPanelWidth() - ts, ts);
    }

    private void drawInformation(SpriteBatch batch) {
        int gold = getManager().getGame().getCurrentPlayer().getGold();
        int current_pop = getManager().getGame().getCurrentPlayer().getPopulation();
        int max_pop = getManager().getGame().getRule().getInteger(UNIT_CAPACITY);
        //draw population
        batch.draw(AER.resources.getStatusHudIcon(0), ts + margin_left, margin_bottom, hud_size, hud_size);
        AER.font.drawLFraction(batch, current_pop, max_pop, ts + margin_left + hud_size, margin_bottom);
        //draw gold
        batch.draw(AER.resources.getStatusHudIcon(1),
                ts + margin_left * 2 + hud_size + max_pop_width, margin_bottom, hud_size, hud_size);
        AER.font.drawLNumber(batch, gold, ts + margin_left * 2 + hud_size * 2 + max_pop_width, margin_bottom);
        batch.flush();
    }

    private void drawSelectedTile(SpriteBatch batch) {
        int cursor_x = screen.getCursorMapX();
        int cursor_y = screen.getCursorMapY();
        if (cursor_x >= 0 && cursor_y >= 0) {
            short tile_index = getManager().getGame().getMap().getTileIndex(cursor_x, cursor_y);
            batch.draw(AER.resources.getTileTexture(tile_index), 0, 0, ts, ts);
            //draw defence bonus
            AER.font.drawTileDefenceBonus(
                    batch, getManager().getGame().getMap().getTile(cursor_x, cursor_y).getDefenceBonus(),
                    (ts - AER.font.getSNumberWidth(99, true)) / 2, ts / 12);
        }
        batch.flush();
    }

}
