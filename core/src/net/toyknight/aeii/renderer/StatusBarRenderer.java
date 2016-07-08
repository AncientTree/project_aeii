package net.toyknight.aeii.renderer;

import static net.toyknight.aeii.entity.Rule.Entry.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.manager.GameManager;
import net.toyknight.aeii.screen.GameScreen;

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
        this.max_pop_width = getContext().getFontRenderer().getLNumberWidth(9999, false);
        int max_gold_width = getContext().getFontRenderer().getLFractionWidth(99, 99);
        this.margin_left = (screen.getViewportWidth() - ts - max_pop_width - max_gold_width - hud_size * 2) / 3;
        this.margin_bottom = (ts - hud_size) / 2;
    }

    private GameContext getContext() {
        return screen.getContext();
    }

    private GameManager getManager() {
        return screen.getGameManager();
    }

    public void drawStatusBar(SpriteBatch batch) {
        int current_team = getManager().getGame().getCurrentTeam();
        batch.draw(getContext().getResources().getTeamBackground(current_team),
                0, 0, Gdx.app.getGraphics().getWidth() - screen.getRightPanelWidth(), ts);
        batch.flush();
        drawSelectedTile(batch);
        drawInformation(batch);
        getContext().getBorderRenderer().drawBorder(batch, 0, 0, ts, ts);
        getContext().getBorderRenderer().drawBorder(
                batch, ts, 0, Gdx.app.getGraphics().getWidth() - screen.getRightPanelWidth() - ts, ts);
    }

    private void drawInformation(SpriteBatch batch) {
        int gold = getManager().getGame().getCurrentPlayer().getGold();
        int current_pop = getManager().getGame().getCurrentPlayer().getPopulation();
        int max_pop = getManager().getGame().getRule().getInteger(MAX_POPULATION);
        //draw population
        batch.draw(getContext().getResources().getStatusHudIcon(0), ts + margin_left, margin_bottom, hud_size, hud_size);
        getContext().getFontRenderer().drawLFraction(batch, current_pop, max_pop, ts + margin_left + hud_size, margin_bottom);
        //draw gold
        batch.draw(getContext().getResources().getStatusHudIcon(1),
                ts + margin_left * 2 + hud_size + max_pop_width, margin_bottom, hud_size, hud_size);
        getContext().getFontRenderer().drawLNumber(batch, gold, ts + margin_left * 2 + hud_size * 2 + max_pop_width, margin_bottom);
        batch.flush();
    }

    private void drawSelectedTile(SpriteBatch batch) {
        int cursor_x = screen.getCursorMapX();
        int cursor_y = screen.getCursorMapY();
        if (cursor_x >= 0 && cursor_y >= 0) {
            short tile_index = getManager().getGame().getMap().getTileIndex(cursor_x, cursor_y);
            batch.draw(getContext().getResources().getTileTexture(tile_index), 0, 0, ts, ts);
            //draw defence bonus
            getContext().getFontRenderer().drawTileDefenceBonus(
                    batch, getManager().getGame().getMap().getTile(cursor_x, cursor_y).getDefenceBonus(),
                    (ts - getContext().getFontRenderer().getSNumberWidth(99, true)) / 2, ts / 12);
        }
        batch.flush();
    }

}
