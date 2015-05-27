package com.toyknight.aeii.rule;

import com.toyknight.aeii.utils.UnitFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by toyknight on 4/15/2015.
 */
public class Rule {

    private int poison_damage;

    private int kill_exp;
    private int attack_exp;
    private int counter_exp;

    private int commander_price_growth;

    private int max_population;

    private HashMap<String, ArrayList<Integer>> available_unit_list;

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

    public void addAvailableUnits(String package_name, ArrayList<Integer> list) {
        this.available_unit_list.put(package_name, list);
    }

    public HashMap<String, ArrayList<Integer>> getAvailableUnitList() {
        return available_unit_list;
    }

    public static Rule getDefaultRule() {
        Rule rule = new Rule();
        rule.setPoisonDamage(10);
        rule.setKillExperience(60);
        rule.setAttackExperience(30);
        rule.setCounterExperience(10);
        rule.setCommanderPriceGrowth(500);
        rule.setMaxPopulation(20);

        int skeleton = UnitFactory.getSkeletonIndex();
        int crystal = UnitFactory.getCrystalIndex();
        ArrayList<Integer> unit_list = new ArrayList();
        for (int i = 0; i < UnitFactory.getUnitCount("default"); i++) {
            if (i != skeleton && i != crystal) {
                unit_list.add(i);
            }
        }
        rule.addAvailableUnits("default", unit_list);
        return rule;
    }

}
