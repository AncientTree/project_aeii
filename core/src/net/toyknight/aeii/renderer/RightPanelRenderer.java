package net.toyknight.aeii.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.manager.GameManager;
import net.toyknight.aeii.screen.GameScreen;

/**
 * @author toyknight 4/26/2015.
 */
public class RightPanelRenderer {

    private final int ts;
    private final int pad;
    private final int f_size;
    private final GameScreen screen;

    private Unit target_unit;

    public RightPanelRenderer(final GameScreen screen, int ts) {
        this.ts = ts;
        this.pad = ts / 2;
        this.f_size = screen.getRightPanelWidth() - pad * 2; //unit frame size;
        this.screen = screen;
        Button btn_focus = new Button(screen.getContext().getSkin());
        btn_focus.setBounds(screen.getViewportWidth() + pad, screen.getViewportHeight() - pad - f_size, f_size, f_size);
        btn_focus.setStyle(new Button.ButtonStyle());
        btn_focus.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (target_unit != null) {
                    RightPanelRenderer.this.screen.locateViewport(target_unit.getX(), target_unit.getY());
                }
            }
        });
        screen.addActor(btn_focus);
    }

    public GameContext getContext() {
        return screen.getContext();
    }

    public void drawStatusBar(Batch batch) {
        batch.draw(getContext().getResources().getPanelBackground(),
                Gdx.graphics.getWidth() - screen.getRightPanelWidth(), 0,
                screen.getRightPanelWidth(), Gdx.graphics.getHeight());
        getContext().getBorderRenderer().drawBorder(batch,
                Gdx.graphics.getWidth() - screen.getRightPanelWidth(), ts,
                screen.getRightPanelWidth(), Gdx.graphics.getHeight() - ts * 2);
        drawInformation(batch);
        batch.flush();
    }

    private GameManager getManager() {
        return screen.getGameManager();
    }

    private void drawInformation(Batch batch) {
        int hw = ts * 13 / 24;
        int hh = ts * 16 / 24;
        float lbh = getContext().getResources().getTextFont().getCapHeight();
        batch.draw(getContext().getResources().getBorderLightColor(),
                screen.getViewportWidth() + pad, screen.getViewportHeight() - pad - f_size, f_size, f_size);
        batch.draw(getContext().getResources().getListBackground(),
                screen.getViewportWidth() + pad + 2, screen.getViewportHeight() - pad - f_size + 2, f_size - 4, f_size - 4);
        Unit unit = getManager().getGame().getMap().getUnit(screen.getCursorMapX(), screen.getCursorMapY());
        if (unit != null) {
            target_unit = unit;
        }
        String STR_HP = "HP ";
        String STR_EXP = "XP ";
        if (target_unit != null) {
            batch.draw(getContext().getResources().getUnitTexture(target_unit.getTeam(), target_unit.getIndex(), 0),
                    screen.getViewportWidth() + ts, screen.getViewportHeight() - ts * 2, ts, ts);
            if (target_unit.isCommander()) {
                getContext().getCanvasRenderer().drawHead(batch, target_unit.getHead(),
                        screen.getViewportWidth() + ts, screen.getViewportHeight() - ts * 2, 0, ts);
            }
            //draw level
            String level_str = Integer.toString(target_unit.getLevel());
            getContext().getFontRenderer().drawText(batch, level_str,
                    screen.getViewportWidth() + pad + hw + (f_size - hw - getContext().getFontRenderer().getTextLayout(level_str).width) / 2,
                    screen.getViewportHeight() - f_size - pad - (hh - lbh) / 2);
            //draw attack
            String attack_str = Integer.toString(target_unit.getAttack());
            switch (target_unit.getAttackType()) {
                case Unit.ATTACK_PHYSICAL:
                    getContext().getFontRenderer().setTextColor(getContext().getResources().getPhysicalAttackColor());
                    break;
                case Unit.ATTACK_MAGIC:
                    getContext().getFontRenderer().setTextColor(getContext().getResources().getMagicalAttackColor());
                    break;
            }
            getContext().getFontRenderer().drawText(batch, attack_str,
                    screen.getViewportWidth() + pad + hw + (f_size - hw - getContext().getFontRenderer().getTextLayout(attack_str).width) / 2,
                    screen.getViewportHeight() - f_size - pad - hh - (hh - lbh) / 2);
            getContext().getFontRenderer().setTextColor(Color.WHITE);
            //draw physical defence
            String pdefence_str = Integer.toString(target_unit.getPhysicalDefence());
            getContext().getFontRenderer().drawText(batch, pdefence_str,
                    screen.getViewportWidth() + pad + hw + (f_size - hw - getContext().getFontRenderer().getTextLayout(pdefence_str).width) / 2,
                    screen.getViewportHeight() - f_size - pad - hh * 2 - (hh - lbh) / 2);
            //draw magical defence
            String mdefence_str = Integer.toString(target_unit.getMagicDefence());
            getContext().getFontRenderer().drawText(batch, mdefence_str,
                    screen.getViewportWidth() + pad + hw + (f_size - hw - getContext().getFontRenderer().getTextLayout(mdefence_str).width) / 2,
                    screen.getViewportHeight() - f_size - pad - hh * 3 - (hh - lbh) / 2);
            //draw health points
            String hp_str = target_unit.getCurrentHp() + "/" + target_unit.getMaxHp();
            getContext().getFontRenderer().setTextColor(Color.GREEN);
            getContext().getFontRenderer().drawText(batch, hp_str,
                    screen.getViewportWidth() + pad + getContext().getFontRenderer().getTextLayout(STR_HP).width,
                    screen.getViewportHeight() - f_size - pad - hh * 4 - ts / 4);
            //draw experience
            String exp_str = target_unit.getLevelUpExperience() > 0 ?
                    target_unit.getCurrentExperience() + "/" + target_unit.getLevelUpExperience() : "-/-";
            getContext().getFontRenderer().setTextColor(Color.CYAN);
            getContext().getFontRenderer().drawText(batch, exp_str,
                    screen.getViewportWidth() + pad + getContext().getFontRenderer().getTextLayout(STR_EXP).width,
                    screen.getViewportHeight() - f_size - pad - hh * 4 - lbh - ts / 2);
            getContext().getFontRenderer().setTextColor(Color.WHITE);
        }
        batch.draw(getContext().getResources().getBattleHudIcon(3),
                screen.getViewportWidth() + pad, screen.getViewportHeight() - f_size - pad - hh, hw, hh);
        batch.draw(getContext().getResources().getBattleHudIcon(0),
                screen.getViewportWidth() + pad, screen.getViewportHeight() - f_size - pad - hh * 2, hw, hh);
        batch.draw(getContext().getResources().getBattleHudIcon(1),
                screen.getViewportWidth() + pad, screen.getViewportHeight() - f_size - pad - hh * 3, hw, hh);
        batch.draw(getContext().getResources().getBattleHudIcon(2),
                screen.getViewportWidth() + pad, screen.getViewportHeight() - f_size - pad - hh * 4, hw, hh);
        getContext().getFontRenderer().drawText(batch, STR_HP,
                screen.getViewportWidth() + pad, screen.getViewportHeight() - f_size - pad - hh * 4 - ts / 4);
        getContext().getFontRenderer().drawText(batch, STR_EXP,
                screen.getViewportWidth() + pad, screen.getViewportHeight() - f_size - pad - hh * 4 - lbh - ts / 2);

    }

}
