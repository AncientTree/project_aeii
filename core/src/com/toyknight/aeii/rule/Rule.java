package com.toyknight.aeii.rule;

import com.toyknight.aeii.utils.UnitFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by toyknight on 4/15/2015.
 */
public class Rule implements Serializable {

    private static final long serialVersionUID = 04152015L;

    private int poison_damage;

    private int kill_exp;
    private int attack_exp;
    private int counter_exp;

    private int commander_price_growth;

    private int max_population;

    private boolean need_enemy_clear;
    private boolean need_castle_clear;

    private ArrayList<Integer> available_unit_list = new ArrayList<Integer>();

    public void setPoisonDamage(int damage) {
        this.poison_damage = damage;
    }

    public int getPoisonDamage() {
        return poison_damage;
    }

    public void setKillExperience(int exp) {
        this.kill_exp = exp;
    }

    public int getKillExperience() {
        return kill_exp;
    }

    public void setAttackExperience(int exp) {
        this.attack_exp = exp;
    }

    public int getAttackExperience() {
        return attack_exp;
    }

    public void setCounterExperience(int exp) {
        this.counter_exp = exp;
    }

    public int getCounterExperience() {
        return counter_exp;
    }

    public void setCommanderPriceGrowth(int growth) {
        this.commander_price_growth = growth;
    }

    public int getCommanderPriceGrowth() {
        return commander_price_growth;
    }

    public void setMaxPopulation(int population) {
        this.max_population = population;
    }

    public int getMaxPopulation() {
        return max_population;
    }

    public void setEnemyClearNeeded(boolean b) {
        this.need_enemy_clear = b;
    }

    public boolean isEnemyClearNeeded() {
        return need_enemy_clear;
    }

    public void setCastleClearNeeded(boolean b) {
        this.need_castle_clear = b;
    }

    public boolean isCastleClearNeeded() {
        return need_castle_clear;
    }

    public void setAvailableUnits(ArrayList<Integer> list) {
        available_unit_list = list;
    }

    public ArrayList<Integer> getAvailableUnitList() {
        return available_unit_list;
    }


    public static Rule getDefaultRule() {
        Rule rule = new Rule();
        rule.setPoisonDamage(10);
        rule.setKillExperience(60);
        rule.setAttackExperience(30);
        rule.setCounterExperience(10);
        rule.setCommanderPriceGrowth(250);
        rule.setMaxPopulation(20);
        rule.setEnemyClearNeeded(true);
        rule.setCastleClearNeeded(true);

        int commander = UnitFactory.getCommanderIndex();
        int skeleton = UnitFactory.getSkeletonIndex();
        int crystal = UnitFactory.getCrystalIndex();
        ArrayList<Integer> unit_list = new ArrayList<Integer>();
        for (int index = 0; index < UnitFactory.getUnitCount(); index++) {
            if (index != commander && index != skeleton && index != crystal) {
                unit_list.add(index);
            }
        }
        unit_list.add(commander);
        rule.setAvailableUnits(unit_list);
        return rule;
    }

}
