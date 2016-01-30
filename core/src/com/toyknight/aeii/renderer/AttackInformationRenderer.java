package com.toyknight.aeii.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.screen.GameScreen;
import com.toyknight.aeii.utils.UnitToolkit;

/**
 * @author toyknight 6/3/2015.
 */
public class AttackInformationRenderer {

    private final int ts;
    private final GameScreen screen;

    public AttackInformationRenderer(GameScreen screen) {
        this.screen = screen;
        this.ts = screen.getContext().getTileSize();
    }

    private GameScreen getGameScreen() {
        return screen;
    }

    private GameManager getManager() {
        return screen.getManager();
    }

    private UnitToolkit getUnitToolkit() {
        return getManager().getUnitToolkit();
    }

    public void render(Batch batch) {
        if (getManager().getState() == GameManager.STATE_ATTACK && getGameScreen().canOperate()) {
            int cursor_x = screen.getCursorMapX();
            int cursor_y = screen.getCursorMapY();
            Unit attacker = getManager().getSelectedUnit();
            Unit defender = getManager().getGame().getMap().getUnit(cursor_x, cursor_y);
            if (attacker != null && defender != null && getManager().getGame().canAttack(attacker, defender)) {
                drawInformation(batch, attacker, defender);
            }
        }
    }

    private void drawInformation(Batch batch, Unit attacker, Unit defender) {
        int aw = ts * 9 / 24;
        int ah = ts * 7 / 24; // arrow height
        int hw = ts * 13 / 24; //hud icon width
        int hh = ts * 16 / 24; //hud icon height
        int tfw = ts * 2; //text field width
        float lbh = FontRenderer.getTextFont().getCapHeight(); //label height
        int tfh = FontRenderer.getSCharHeight() + ts / 12; //text field height
        int lmargin = (screen.getViewportWidth() - hw * 4 - tfw * 4 - ts * 7 / 24) / 2; //margin left
        int infoh = tfh * 2 + ts * 3 / 24; //information panel height
        int cursor_sy = screen.getYOnScreen(screen.getCursorMapY());
        int infoy = cursor_sy > (screen.getViewportHeight() - ts) / 2 + ts ?
                ts + ts / 12 : ts + screen.getViewportHeight() - infoh - ts / 12; //information panel y

        //draw background
        batch.draw(ResourceManager.getBorderDarkColor(), 0, infoy - ts / 24, screen.getViewportWidth(), infoh);
        batch.draw(ResourceManager.getBorderLightColor(), 0, infoy, screen.getViewportWidth(), infoh);

        //draw icons
        batch.draw(ResourceManager.getBattleHudIcon(0), lmargin, infoy + (infoh - hh) / 2, hw, hh);
        batch.draw(ResourceManager.getBattleHudIcon(1), lmargin + hw + tfw + ts * 2 / 24, infoy + (infoh - hh) / 2, hw, hh);
        batch.draw(ResourceManager.getBattleHudIcon(2), lmargin + hw * 2 + tfw * 2 + ts * 4 / 24, infoy + (infoh - hh) / 2, hw, hh);
        batch.draw(ResourceManager.getBattleHudIcon(3), lmargin + hw * 3 + tfw * 3 + ts * 6 / 24, infoy + (infoh - hh) / 2, hw, hh);

        //draw text field
        batch.draw(ResourceManager.getPanelBackground(), lmargin + hw + ts / 24, infoy + ts / 24, tfw, tfh);
        batch.draw(ResourceManager.getPanelBackground(), lmargin + hw + ts / 24, infoy + tfh + ts * 2 / 24, tfw, tfh);
        batch.draw(ResourceManager.getPanelBackground(), lmargin + hw * 2 + tfw + ts * 3 / 24, infoy + ts / 24, tfw, tfh);
        batch.draw(ResourceManager.getPanelBackground(), lmargin + hw * 2 + tfw + ts * 3 / 24, infoy + tfh + ts * 2 / 24, tfw, tfh);
        batch.draw(ResourceManager.getPanelBackground(), lmargin + hw * 3 + tfw * 2 + ts * 5 / 24, infoy + ts / 24, tfw, tfh);
        batch.draw(ResourceManager.getPanelBackground(), lmargin + hw * 3 + tfw * 2 + ts * 5 / 24, infoy + tfh + ts * 2 / 24, tfw, tfh);
        batch.draw(ResourceManager.getPanelBackground(), lmargin + hw * 4 + tfw * 3 + ts * 7 / 24, infoy + ts / 24, tfw, tfh);
        batch.draw(ResourceManager.getPanelBackground(), lmargin + hw * 4 + tfw * 3 + ts * 7 / 24, infoy + tfh + ts * 2 / 24, tfw, tfh);

        batch.draw(ResourceManager.getTeamBackground(attacker.getTeam()), lmargin + hw + ts * 2 / 24, infoy + ts * 2 / 24, ts / 4, FontRenderer.getSCharHeight());
        batch.draw(ResourceManager.getTeamBackground(attacker.getTeam()), lmargin + hw * 2 + tfw + ts * 4 / 24, infoy + ts * 2 / 24, ts / 4, FontRenderer.getSCharHeight());
        batch.draw(ResourceManager.getTeamBackground(attacker.getTeam()), lmargin + hw * 3 + tfw * 2 + ts * 6 / 24, infoy + ts * 2 / 24, ts / 4, FontRenderer.getSCharHeight());
        batch.draw(ResourceManager.getTeamBackground(attacker.getTeam()), lmargin + hw * 4 + tfw * 3 + ts * 8 / 24, infoy + ts * 2 / 24, ts / 4, FontRenderer.getSCharHeight());

        batch.draw(ResourceManager.getTeamBackground(defender.getTeam()), lmargin + hw + ts * 2 / 24, infoy + tfh + ts * 3 / 24, ts / 4, FontRenderer.getSCharHeight());
        batch.draw(ResourceManager.getTeamBackground(defender.getTeam()), lmargin + hw * 2 + tfw + ts * 4 / 24, infoy + tfh + ts * 3 / 24, ts / 4, FontRenderer.getSCharHeight());
        batch.draw(ResourceManager.getTeamBackground(defender.getTeam()), lmargin + hw * 3 + tfw * 2 + ts * 6 / 24, infoy + tfh + ts * 3 / 24, ts / 4, FontRenderer.getSCharHeight());
        batch.draw(ResourceManager.getTeamBackground(defender.getTeam()), lmargin + hw * 4 + tfw * 3 + ts * 8 / 24, infoy + tfh + ts * 3 / 24, ts / 4, FontRenderer.getSCharHeight());

        //get tiles
        int attacker_tile = getManager().getGame().getMap().getTileIndex(attacker.getX(), attacker.getY());
        int defender_tile = getManager().getGame().getMap().getTileIndex(defender.getX(), defender.getY());

        //draw attack
        switch (attacker.getAttackType()) {
            case Unit.ATTACK_PHYSICAL:
                FontRenderer.setTextColor(ResourceManager.getPhysicalAttackColor());
                break;
            case Unit.ATTACK_MAGIC:
                FontRenderer.setTextColor(ResourceManager.getMagicalAttackColor());
                break;
        }
        int attacker_atk = attacker.getAttack();
        int attacker_atk_bonus = getUnitToolkit().getAttackBonus(attacker, defender, attacker_tile);
        String attacker_attack_str = Integer.toString(attacker_atk + attacker_atk_bonus);
        FontRenderer.drawText(batch, attacker_attack_str,
                lmargin + hw + ts * 3 / 24 + ts / 4,
                infoy + ts / 24 + (tfh - lbh) / 2 + lbh);
        if (attacker_atk_bonus > 0) {
            float attack_width = FontRenderer.getTextLayout(attacker_attack_str).width;
            batch.draw(ResourceManager.getArrowIcon(1),
                    lmargin + hw + ts * 4 / 24 + ts / 4 + attack_width,
                    infoy + ts / 24 + (tfh - ah) / 2,
                    aw, ah);
        }

        if (attacker_atk_bonus < 0) {
            float attack_width = FontRenderer.getTextLayout(attacker_attack_str).width;
            batch.draw(ResourceManager.getArrowIcon(2),
                    lmargin + hw + ts * 4 / 24 + ts / 4 + attack_width,
                    infoy + ts / 24 + (tfh - ah) / 2,
                    aw, ah);
        }

        switch (defender.getAttackType()) {
            case Unit.ATTACK_PHYSICAL:
                FontRenderer.setTextColor(ResourceManager.getPhysicalAttackColor());
                break;
            case Unit.ATTACK_MAGIC:
                FontRenderer.setTextColor(ResourceManager.getMagicalAttackColor());
                break;
        }
        int defender_atk = defender.getAttack();
        int defender_atk_bonus = getUnitToolkit().getAttackBonus(defender, attacker, defender_tile);
        int modified_defender_atk = getUnitToolkit().canCounter(defender, attacker) ? defender_atk + defender_atk_bonus : 0;
        String defender_attack_str = Integer.toString(modified_defender_atk);
        FontRenderer.drawText(batch, defender_attack_str,
                lmargin + hw + ts * 3 / 24 + ts / 4,
                infoy + tfh + ts * 2 / 24 + (tfh - lbh) / 2 + lbh);
        if (modified_defender_atk > 0 && defender_atk_bonus > 0) {
            float attack_width = FontRenderer.getTextLayout(defender_attack_str).width;
            batch.draw(ResourceManager.getArrowIcon(1),
                    lmargin + hw + ts * 4 / 24 + ts / 4 + attack_width,
                    infoy + tfh + ts * 2 / 24 + (tfh - ah) / 2,
                    aw, ah);
        }

        if (modified_defender_atk > 0 && defender_atk_bonus < 0) {
            float attack_width = FontRenderer.getTextLayout(defender_attack_str).width;
            batch.draw(ResourceManager.getArrowIcon(2),
                    lmargin + hw + ts * 4 / 24 + ts / 4 + attack_width,
                    infoy + tfh + ts * 2 / 24 + (tfh - ah) / 2,
                    aw, ah);
        }

        //paint defence
        FontRenderer.setTextColor(Color.WHITE);
        int attacker_p_defence_bonus = getUnitToolkit().getPhysicalDefenceBonus(defender, attacker, attacker_tile);
        int attacker_p_defence = attacker.getPhysicalDefence();
        String attacker_p_defence_str = Integer.toString(attacker_p_defence + attacker_p_defence_bonus);
        FontRenderer.drawText(batch, attacker_p_defence_str,
                lmargin + hw * 2 + tfw + ts * 5 / 24 + ts / 4,
                infoy + ts / 24 + (tfh - lbh) / 2 + lbh);
        int attacker_m_defence_bonus = getUnitToolkit().getMagicDefenceBonus(defender, attacker, attacker_tile);
        int attacker_m_defence = attacker.getMagicDefence();
        String attacker_m_defence_str = Integer.toString(attacker_m_defence + attacker_m_defence_bonus);
        FontRenderer.drawText(batch, attacker_m_defence_str,
                lmargin + hw * 3 + tfw * 2 + 7 * ts / 24 + ts / 4,
                infoy + ts / 24 + (tfh - lbh) / 2 + lbh);

        if (attacker_p_defence_bonus > 0) {
            float p_defence_width = FontRenderer.getTextLayout(attacker_p_defence_str).width;
            batch.draw(ResourceManager.getArrowIcon(1),
                    lmargin + hw * 2 + tfw + 6 * ts / 24 + ts / 4 + p_defence_width,
                    infoy + ts / 24 + (tfh - ah) / 2,
                    aw, ah);

        }
        if (attacker_p_defence_bonus < 0) {
            float p_defence_width = FontRenderer.getTextLayout(attacker_p_defence_str).width;
            batch.draw(ResourceManager.getArrowIcon(2),
                    lmargin + hw * 2 + tfw + 6 * ts / 24 + ts / 4 + p_defence_width,
                    infoy + ts / 24 + (tfh - ah) / 2,
                    aw, ah);

        }
        if (attacker_m_defence_bonus > 0) {
            float m_defence_width = FontRenderer.getTextLayout(attacker_m_defence_str).width;
            batch.draw(ResourceManager.getArrowIcon(1),
                    lmargin + hw * 3 + tfw * 2 + 8 * ts / 24 + ts / 4 + m_defence_width,
                    infoy + ts / 24 + (tfh - ah) / 2,
                    aw, ah);
        }
        if (attacker_m_defence_bonus < 0) {
            float m_defence_width = FontRenderer.getTextLayout(attacker_m_defence_str).width;
            batch.draw(ResourceManager.getArrowIcon(2),
                    lmargin + hw * 3 + tfw * 2 + 8 * ts / 24 + ts / 4 + m_defence_width,
                    infoy + ts / 24 + (tfh - ah) / 2,
                    aw, ah);
        }

        int defender_p_defence_bonus = getUnitToolkit().getPhysicalDefenceBonus(attacker, defender, defender_tile);
        int defender_p_defence = defender.getPhysicalDefence();
        String defender_p_defence_str = Integer.toString(defender_p_defence + defender_p_defence_bonus);
        FontRenderer.drawText(batch, defender_p_defence_str,
                lmargin + hw * 2 + tfw + 5 * ts / 24 + ts / 4,
                infoy + tfh + 2 * ts / 24 + (tfh - lbh) / 2 + lbh);
        int defender_m_defence_bonus = getUnitToolkit().getMagicDefenceBonus(attacker, defender, defender_tile);
        int defender_m_defence = defender.getMagicDefence();
        String defender_m_defence_str = Integer.toString(defender_m_defence + defender_m_defence_bonus);
        FontRenderer.drawText(batch, defender_m_defence_str,
                lmargin + hw * 3 + tfw * 2 + 7 * ts / 24 + ts / 4,
                infoy + tfh + 2 * ts / 24 + (tfh - lbh) / 2 + lbh);
        if (defender_p_defence_bonus > 0) {
            float p_defence_width = FontRenderer.getTextLayout(defender_p_defence_str).width;
            batch.draw(ResourceManager.getArrowIcon(1),
                    lmargin + hw * 2 + tfw + 6 * ts / 24 + ts / 4 + p_defence_width,
                    infoy + tfh + 2 * ts / 24 + (tfh - ah) / 2,
                    aw, ah);

        }
        if (defender_m_defence_bonus < 0) {
            float p_defence_width = FontRenderer.getTextLayout(defender_p_defence_str).width;
            batch.draw(ResourceManager.getArrowIcon(2),
                    lmargin + hw * 2 + tfw + 6 * ts / 24 + ts / 4 + p_defence_width,
                    infoy + tfh + 2 * ts / 24 + (tfh - ah) / 2,
                    aw, ah);
        }
        if (defender_m_defence_bonus > 0) {
            float m_defence_width = FontRenderer.getTextLayout(defender_m_defence_str).width;
            batch.draw(ResourceManager.getArrowIcon(1),
                    lmargin + hw * 3 + tfw * 2 + 8 * ts / 24 + ts / 4 + m_defence_width,
                    infoy + tfh + 2 * ts / 24 + (tfh - ah) / 2,
                    aw, ah);
        }
        if (defender_m_defence_bonus < 0) {
            float m_defence_width = FontRenderer.getTextLayout(defender_m_defence_str).width;
            batch.draw(ResourceManager.getArrowIcon(2),
                    lmargin + hw * 3 + tfw * 2 + 8 * ts / 24 + ts / 4 + m_defence_width,
                    infoy + tfh + 2 * ts / 24 + (tfh - ah) / 2,
                    aw, ah);
        }

        //draw level
        int attacker_level = attacker.getLevel();
        FontRenderer.drawText(batch, Integer.toString(attacker_level),
                lmargin + hw * 4 + tfw * 3 + 9 * ts / 24 + ts / 4,
                infoy + ts / 24 + (tfh - lbh) / 2 + lbh);
        int defender_level = defender.getLevel();
        FontRenderer.drawText(batch, Integer.toString(defender_level),
                lmargin + hw * 4 + tfw * 3 + 9 * ts / 24 + ts / 4,
                infoy + tfh + 2 * ts / 24 + (tfh - lbh) / 2 + lbh);
        batch.flush();
    }

}
