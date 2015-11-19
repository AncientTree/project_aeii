package com.toyknight.aeii.rule;

import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.utils.UnitFactory;

import java.io.Serializable;

/**
 * @author toyknight 4/15/2015.
 */
public class Rule implements Serializable {

    private static final long serialVersionUID = 4152015L;

    public static int POISON_DAMAGE = 10;
    public static int HEALER_BASE_HEAL = 40;
    public static int REFRESH_BASE_HEAL = 10;

    private int castle_income;
    private int village_income;
    private int commander_income;

    private int kill_exp;
    private int attack_exp;
    private int counter_exp;

    private int commander_price_growth;

    private int max_population;

    private boolean need_enemy_clear;
    private boolean need_castle_clear;

    private Array<Integer> available_unit_list = new Array<Integer>();

    public Rule() {
    }

    public Rule(Rule rule) {
        setCastleIncome(rule.getCastleIncome());
        setVillageIncome(rule.getVillageIncome());
        setCommanderIncome(rule.getCommanderIncome());
        setKillExperience(rule.getKillExperience());
        setAttackExperience(rule.getAttackExperience());
        setCounterExperience(rule.getCounterExperience());
        setCommanderPriceGrowth(rule.getCommanderPriceGrowth());
        setMaxPopulation(rule.getMaxPopulation());
        setEnemyClearNeeded(rule.isEnemyClearNeeded());
        setCastleClearNeeded(rule.isCastleClearNeeded());
        setAvailableUnits(new Array<Integer>(rule.getAvailableUnitList()));
    }

    public void setCastleIncome(int income) {
        this.castle_income = income;
    }

    public int getCastleIncome() {
        return castle_income;
    }

    public void setVillageIncome(int income) {
        this.village_income = income;
    }

    public int getVillageIncome() {
        return village_income;
    }

    public void setCommanderIncome(int income) {
        this.commander_income = income;
    }

    public int getCommanderIncome() {
        return commander_income;
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

    public void setAvailableUnits(Array<Integer> list) {
        available_unit_list = list;
    }

    public Array<Integer> getAvailableUnitList() {
        return available_unit_list;
    }

    public static Rule getDefaultRule() {
        Rule rule = new Rule();
        rule.setCastleIncome(100);
        rule.setVillageIncome(50);
        rule.setCommanderIncome(50);
        rule.setKillExperience(60);
        rule.setAttackExperience(30);
        rule.setCounterExperience(10);
        rule.setCommanderPriceGrowth(100);
        rule.setMaxPopulation(20);
        rule.setEnemyClearNeeded(true);
        rule.setCastleClearNeeded(true);

        int commander = UnitFactory.getCommanderIndex();
        int skeleton = UnitFactory.getSkeletonIndex();
        int crystal = UnitFactory.getCrystalIndex();
        Array<Integer> unit_list = new Array<Integer>();
        for (int index = 0; index < UnitFactory.getUnitCount(); index++) {
            if (index != commander && index != skeleton && index != crystal) {
                unit_list.add(index);
            }
        }
        sortUnitList(unit_list).add(commander);
        rule.setAvailableUnits(unit_list);
        return rule;
    }

    private static Array<Integer> sortUnitList(Array<Integer> list) {
        for (int i = list.size - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (UnitFactory.getSample(list.get(j)).getPrice() > UnitFactory.getSample(list.get(j + 1)).getPrice()) {
                    list.swap(j, j + 1);
                }
            }
        }
        return list;
    }

}
