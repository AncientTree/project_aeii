package com.toyknight.aeii.entity;

import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.utils.UnitFactory;

import java.io.Serializable;

/**
 * @author toyknight 4/3/2015.
 */
public class Unit implements Serializable {

    private static final long serialVersionUID = 4032015L;

    public static final int ATTACK_PHYSICAL = 0;
    public static final int ATTACK_MAGICAL = 1;
    private static final int[] level_up_experience = {100, 150, 300};

    private final int index;

    private int price;

    private int level;
    private int experience = 0;

    private final String unit_code;
    private int team;

    private int max_hp;
    private int current_hp;
    private int attack;
    private int attack_type;
    private int physical_defence;
    private int magic_defence;
    private int movement_point;
    private int current_movement_point;

    private Array<Integer> abilities;
    private Status status;

    private int hp_growth;
    private int attack_growth;
    private int physical_defence_growth;
    private int magic_defence_growth;
    private int movement_growth;

    private int x_position;
    private int y_position;

    private int max_attack_range;
    private int min_attack_range;

    private boolean is_standby;

    public Unit() {
        this(-1);
    }

    public Unit(int index, String unit_code) {
        this.level = 0;
        this.index = index;
        this.abilities = new Array<Integer>();
        this.unit_code = unit_code;
        this.is_standby = false;
    }

    public Unit(int index) {
        this(index, "#");
    }

    public Unit(Unit unit, String unit_code) {
        this(unit.getIndex(), unit_code);
        this.level = unit.getLevel();
        this.experience = unit.getTotalExperience();
        this.price = unit.getPrice();
        this.team = unit.getTeam();
        this.max_hp = unit.getMaxHp();
        this.current_hp = unit.getCurrentHp();
        this.attack = unit.getAttack();
        this.attack_type = unit.getAttackType();
        this.physical_defence = unit.getPhysicalDefence();
        this.magic_defence = unit.getMagicDefence();
        this.movement_point = unit.getMovementPoint();
        this.current_movement_point = unit.getCurrentMovementPoint();
        this.hp_growth = unit.getHpGrowth();
        this.attack_growth = unit.getAttackGrowth();
        this.physical_defence_growth = unit.getPhysicalDefenceGrowth();
        this.magic_defence_growth = unit.getMagicDefenceGrowth();
        this.movement_growth = unit.getMovementGrowth();
        this.x_position = unit.getX();
        this.y_position = unit.getY();
        this.max_attack_range = unit.getMaxAttackRange();
        this.min_attack_range = unit.getMinAttackRange();
        this.abilities = new Array<Integer>(unit.getAbilities());
        this.status = unit.getStatus();
    }

    public Unit(UnitDefinition definition, int index) {
        this(index);
        this.price = definition.price;
        this.max_hp = definition.max_hp;
        this.attack = definition.attack;
        this.attack_type = definition.attack_type;
        this.physical_defence = definition.physical_defence;
        this.magic_defence = definition.magic_defence;
        this.movement_point = definition.movement_point;
        this.abilities = definition.abilities;
        this.hp_growth = definition.hp_growth;
        this.attack_growth = definition.attack_growth;
        this.physical_defence_growth = definition.physical_defence_growth;
        this.magic_defence_growth = definition.magic_defence_growth;
        this.movement_growth = definition.movement_growth;
        this.max_attack_range = definition.max_attack_range;
        this.min_attack_range = definition.min_attack_range;
    }

    public int getIndex() {
        return index;
    }

    public boolean isCommander() {
        return getIndex() == UnitFactory.getCommanderIndex();
    }

    public boolean isSkeleton() {
        return getIndex() == UnitFactory.getSkeletonIndex();
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getUnitCode() {
        return unit_code;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public int getMaxHp() {
        return max_hp;
    }

    public int getCurrentHp() {
        return current_hp;
    }

    public void setCurrentHp(int current_hp) {
        this.current_hp = current_hp;
    }

    public void changeCurrentHp(int change) {
        this.current_hp += change;
    }

    public int getAttack() {
        return attack;
    }

    public int getAttackType() {
        return attack_type;
    }

    public int getPhysicalDefence() {
        return physical_defence;
    }

    public int getMagicDefence() {
        return magic_defence;
    }

    public int getMovementGrowth() {
        return movement_growth;
    }

    public int getMovementPoint() {
        if (hasStatus(Status.SLOWED)) {
            return 1;
        } else {
            return movement_point;
        }
    }

    public int getCurrentMovementPoint() {
        return current_movement_point;
    }

    public void setCurrentMovementPoint(int current_movement_point) {
        this.current_movement_point = current_movement_point;
    }

    public boolean hasAbility(int ability) {
        return abilities.indexOf(ability, false) >= 0;
    }

    private Array<Integer> getAbilities() {
        return abilities;
    }

    public void setAbilities(Array<Integer> abilities) {
        this.abilities = abilities;
    }

    public Status getStatus() {
        return status;
    }

    public boolean hasStatus(int type) {
        return getStatus() != null && getStatus().getType() == type;
    }

    public int getHpGrowth() {
        return hp_growth;
    }

    public int getAttackGrowth() {
        return attack_growth;
    }

    public int getPhysicalDefenceGrowth() {
        return physical_defence_growth;
    }

    public int getMagicDefenceGrowth() {
        return magic_defence_growth;
    }

    public int getX() {
        return x_position;
    }

    public void setX(int x_position) {
        this.x_position = x_position;
    }

    public int getY() {
        return y_position;
    }

    public void setY(int y_position) {
        this.y_position = y_position;
    }

    public int getMaxAttackRange() {
        if (hasStatus(Status.BLINDED)) {
            return 0;
        } else {
            return max_attack_range;
        }
    }

    public int getMinAttackRange() {
        if (hasStatus(Status.BLINDED)) {
            return 0;
        } else {
            return min_attack_range;
        }
    }

    public void setStandby(boolean b) {
        this.is_standby = b;
    }

    public boolean isStandby() {
        return is_standby;
    }

    protected void levelUp() {
        if (level < 3) {
            level++;
            this.attack += this.getAttackGrowth();
            this.max_hp += this.getHpGrowth();
            this.changeCurrentHp(getHpGrowth());
            this.movement_point += this.getMovementGrowth();
            this.physical_defence += this.getPhysicalDefenceGrowth();
            this.magic_defence += this.getMagicDefenceGrowth();
        }
    }

    /**
     * @param exp experience
     * @return returns if unit level is up after gaining exp
     */
    public boolean gainExperience(int exp) {
        if (level < 3) {
            boolean level_up_flag = false;
            if (getLevelUpExperience() - getCurrentExperience() <= exp) {
                level_up_flag = true;
                levelUp();
            }
            experience += exp;
            return level_up_flag;
        } else {
            return false;
        }
    }

    public int getTotalExperience() {
        return experience;
    }

    public int getCurrentExperience() {
        int exp = experience;
        for (int i = 0; i < level; i++) {
            exp -= level_up_experience[i];
        }
        return exp;
    }

    public int getLevelUpExperience() {
        if (0 <= level && level < 3) {
            return level_up_experience[level];
        } else {
            return -1;
        }
    }

    public void clearStatus() {
        this.status = null;
    }

    public void attachStatus(Status status) {
        if ((getStatus() == null || getStatus().equals(status)) && !hasAbility(Ability.HEAVY_MACHINE)) {
            if (Status.isBuff(status) && !hasAbility(Ability.CHARGER)) {
                this.status = status;
            }
            if (Status.isDebuff(status)) {
                if (hasAbility(Ability.CHARGER)) {
                    int turn = status.getRemainingTurn();
                    status.setRemainingTurn(turn + 1);
                }
                this.status = status;
            }
            if (status.getType() == Status.SLOWED) {
                if (getCurrentMovementPoint() > 0) {
                    setCurrentMovementPoint(1);
                }
            }
        }
    }

    public void updateStatus() {
        if (status != null) {
            status.update();
            if (status.getRemainingTurn() < 0) {
                status = null;
            }
        }
    }

    public boolean isAt(int x, int y) {
        return this.x_position == x && this.y_position == y;
    }

    public String getVerificationString() {
        String str = "";
        str = str
                + index
                + price
                + max_hp
                + attack
                + attack_type
                + physical_defence
                + magic_defence
                + movement_point
                + hp_growth
                + attack_growth
                + physical_defence_growth
                + magic_defence_growth
                + movement_growth
                + max_attack_range
                + min_attack_range;
        for (Integer ability : abilities) {
            str += ability;
        }
        return str;
    }

    public static class UnitDefinition {

        public int price;

        public int max_hp;

        public int attack;

        public int attack_type;

        public int physical_defence;

        public int magic_defence;

        public int movement_point;

        public Array<Integer> abilities;

        public int hp_growth;

        public int attack_growth;

        public int physical_defence_growth;

        public int magic_defence_growth;

        public int movement_growth;

        public int max_attack_range;

        public int min_attack_range;

    }

}
