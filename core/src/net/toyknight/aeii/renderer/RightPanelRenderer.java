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
import net.toyknight.aeii.gui.GameScreen;
import net.toyknight.aeii.system.AER;

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
        batch.draw(AER.resources.getPanelBackground(),
                Gdx.graphics.getWidth() - screen.getRightPanelWidth(), 0,
                screen.getRightPanelWidth(), Gdx.graphics.getHeight());
        BorderRenderer.drawBorder(batch,
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
        float lbh = AER.resources.getTextFont().getCapHeight();
        batch.draw(AER.resources.getBorderLightColor(),
                screen.getViewportWidth() + pad, screen.getViewportHeight() - pad - f_size, f_size, f_size);
        batch.draw(AER.resources.getListBackground(),
                screen.getViewportWidth() + pad + 2, screen.getViewportHeight() - pad - f_size + 2, f_size - 4, f_size - 4);
        Unit unit = getManager().getGame().getMap().getUnit(screen.getCursorMapX(), screen.getCursorMapY());
        if (unit != null) {
            target_unit = unit;
        }
        String STR_HP = "HP ";
        String STR_EXP = "XP ";
        if (target_unit != null) {
            batch.draw(AER.resources.getUnitTexture(target_unit.getTeam(), target_unit.getIndex(), 0),
                    screen.getViewportWidth() + ts, screen.getViewportHeight() - ts * 2, ts, ts);
            if (target_unit.isCommander()) {
                CanvasRenderer.drawHead_(batch, target_unit.getHead(),
                        screen.getViewportWidth() + ts, screen.getViewportHeight() - ts * 2, 0, ts);
            }
            //draw level
            String level_str = Integer.toString(target_unit.getLevel());
            AER.font.drawText(batch, level_str,
                    screen.getViewportWidth() + pad + hw + (f_size - hw - AER.font.getTextLayout(level_str).width) / 2,
                    screen.getViewportHeight() - f_size - pad - (hh - lbh) / 2);
            //draw attack
            String attack_str = Integer.toString(target_unit.getAttack());
            switch (target_unit.getAttackType()) {
                case Unit.ATTACK_PHYSICAL:
                    AER.font.setTextColor(AER.resources.getPhysicalAttackColor());
                    break;
                case Unit.ATTACK_MAGIC:
                    AER.font.setTextColor(AER.resources.getMagicalAttackColor());
                    break;
            }
            AER.font.drawText(batch, attack_str,
                    screen.getViewportWidth() + pad + hw + (f_size - hw - AER.font.getTextLayout(attack_str).width) / 2,
                    screen.getViewportHeight() - f_size - pad - hh - (hh - lbh) / 2);
            AER.font.setTextColor(Color.WHITE);
            //draw physical defence
            String pdefence_str = Integer.toString(target_unit.getPhysicalDefence());
            AER.font.drawText(batch, pdefence_str,
                    screen.getViewportWidth() + pad + hw + (f_size - hw - AER.font.getTextLayout(pdefence_str).width) / 2,
                    screen.getViewportHeight() - f_size - pad - hh * 2 - (hh - lbh) / 2);
            //draw magical defence
            String mdefence_str = Integer.toString(target_unit.getMagicDefence());
            AER.font.drawText(batch, mdefence_str,
                    screen.getViewportWidth() + pad + hw + (f_size - hw - AER.font.getTextLayout(mdefence_str).width) / 2,
                    screen.getViewportHeight() - f_size - pad - hh * 3 - (hh - lbh) / 2);
            //draw health points
            String hp_str = target_unit.getCurrentHP() + "/" + target_unit.getMaxHP();
            AER.font.setTextColor(Color.GREEN);
            AER.font.drawText(batch, hp_str,
                    screen.getViewportWidth() + pad + AER.font.getTextLayout(STR_HP).width,
                    screen.getViewportHeight() - f_size - pad - hh * 4 - ts / 4);
            //draw experience
            String exp_str = target_unit.getLevelUpExperience() > 0 ?
                    target_unit.getCurrentExperience() + "/" + target_unit.getLevelUpExperience() : "-/-";
            AER.font.setTextColor(Color.CYAN);
            AER.font.drawText(batch, exp_str,
                    screen.getViewportWidth() + pad + AER.font.getTextLayout(STR_EXP).width,
                    screen.getViewportHeight() - f_size - pad - hh * 4 - lbh - ts / 2);
            AER.font.setTextColor(Color.WHITE);
        }
        batch.draw(AER.resources.getBattleHudIcon(3),
                screen.getViewportWidth() + pad, screen.getViewportHeight() - f_size - pad - hh, hw, hh);
        batch.draw(AER.resources.getBattleHudIcon(0),
                screen.getViewportWidth() + pad, screen.getViewportHeight() - f_size - pad - hh * 2, hw, hh);
        batch.draw(AER.resources.getBattleHudIcon(1),
                screen.getViewportWidth() + pad, screen.getViewportHeight() - f_size - pad - hh * 3, hw, hh);
        batch.draw(AER.resources.getBattleHudIcon(2),
                screen.getViewportWidth() + pad, screen.getViewportHeight() - f_size - pad - hh * 4, hw, hh);
        AER.font.drawText(batch, STR_HP,
                screen.getViewportWidth() + pad, screen.getViewportHeight() - f_size - pad - hh * 4 - ts / 4);
        AER.font.drawText(batch, STR_EXP,
                screen.getViewportWidth() + pad, screen.getViewportHeight() - f_size - pad - hh * 4 - lbh - ts / 2);

    }

}
