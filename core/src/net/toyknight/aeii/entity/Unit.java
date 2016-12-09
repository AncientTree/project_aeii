package net.toyknight.aeii.entity;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.Serializable;
import net.toyknight.aeii.Verifiable;
import net.toyknight.aeii.system.AER;
import org.json.JSONObject;

/**
 * @author toyknight 4/3/2015.
 */
public class Unit implements Serializable, Verifiable {

    public static final int ATTACK_PHYSICAL = 0;
    public static final int ATTACK_MAGIC = 1;

    private static final int[] LEVEL_EXPERIENCE = {0, 100, 300, 600};

    private final Definition definition;

    private final int index;

    private String u_code;

    private int team;

    private int level = 0;
    private int experience = 0;

    private int price_increment = 0;

    private int current_hp;
    private int current_movement_point;

    private int x_position;
    private int y_position;

    private Status status;

    private boolean is_standby;

    private boolean is_static;

    private int head = 0;

    public Unit(Definition definition, int index) {
        this.index = index;
        this.definition = definition;
    }

    public Unit(Unit unit) {
        this(unit, unit.getUnitCode());
        setStandby(unit.isStandby());
    }

    public Unit(Unit unit, String u_code) {
        this(unit.getDefinition(), unit.getIndex());
        this.u_code = u_code;
        this.team = unit.getTeam();
        this.level = unit.getLevel();
        this.experience = unit.getTotalExperience();
        this.current_hp = unit.getCurrentHP();
        this.current_movement_point = unit.getCurrentMovementPoint();
        this.x_position = unit.getX();
        this.y_position = unit.getY();
        this.status = unit.getStatus() == null ? null : new Status(unit.getStatus());
        this.is_standby = unit.isStandby();
        this.is_static = unit.isStatic();
        this.head = unit.getHead();
    }

    protected Definition getDefinition() {
        return definition;
    }

    public int getIndex() {
        return index;
    }

    public boolean isCommander() {
        return AER.units.isCommander(getIndex());
    }

    public boolean isCrystal() {
        return AER.units.isCrystal(getIndex());
    }

    public boolean isSkeleton() {
        return AER.units.isSkeleton(getIndex());
    }

    public int getPrice() {
        return definition.price + price_increment;
    }

    public void changePrice(int increment) {
        this.price_increment += increment;
    }

    public int getOccupancy() {
        return definition.occupancy;
    }

    public int getLevel() {
        return level;
    }

    public void setUnitCode(String unit_code) {
        this.u_code = unit_code;
    }

    public String getUnitCode() {
        return u_code;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public int getMaxHP() {
        return definition.max_hp + getHPGrowth() * getLevel();
    }

    public int getCurrentHP() {
        return current_hp;
    }

    public void setCurrentHP(int current_hp) {
        this.current_hp = current_hp;
    }

    public void changeCurrentHP(int change) {
        this.current_hp += change;
    }

    public int getAttack() {
        return definition.attack + getAttackGrowth() * getLevel();
    }

    public int getAttackType() {
        return definition.attack_type;
    }

    public int getPhysicalDefence() {
        return definition.physical_defence + getPhysicalDefenceGrowth() * getLevel();
    }

    public int getMagicDefence() {
        return definition.magic_defence + getMagicDefenceGrowth() * getLevel();
    }

    public int getMovementPoint() {
        if (hasStatus(Status.SLOWED)) {
            return 1;
        } else {
            return definition.movement_point + getMovementGrowth() * getLevel();
        }
    }

    public int getCurrentMovementPoint() {
        return current_movement_point;
    }

    public void setCurrentMovementPoint(int current_movement_point) {
        this.current_movement_point = current_movement_point;
    }

    public void resetMovementPoint() {
        current_movement_point = getMovementPoint();
    }

    public boolean hasAbility(int ability) {
        return definition.abilities.indexOf(ability, false) >= 0;
    }

    public Array<Integer> getAbilities() {
        return definition.abilities;
    }

    public Status getStatus() {
        return status;
    }

    public boolean hasStatus(int type) {
        return getStatus() != null && getStatus().getType() == type;
    }

    public int getHPGrowth() {
        return definition.hp_growth;
    }

    public int getAttackGrowth() {
        return definition.attack_growth;
    }

    public int getPhysicalDefenceGrowth() {
        return definition.physical_defence_growth;
    }

    public int getMagicDefenceGrowth() {
        return definition.magic_defence_growth;
    }

    public int getMovementGrowth() {
        return definition.movement_growth;
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
            return definition.max_attack_range;
        }
    }

    public int getMinAttackRange() {
        if (hasStatus(Status.BLINDED)) {
            return 0;
        } else {
            return definition.min_attack_range;
        }
    }

    public void setStandby(boolean b) {
        this.is_standby = b;
    }

    public boolean isStandby() {
        return is_standby;
    }

    /**
     * @param experience experience
     * @return returns if unit level is up after gaining experience
     */
    public boolean gainExperience(int experience) {
        if (level < 3) {
            int old_level = getLevel();
            int total_experience = getTotalExperience();
            setTotalExperience(total_experience + experience);
            int level_advance = getLevel() - old_level;
            current_hp += getHPGrowth() * level_advance;
            current_movement_point += definition.movement_growth * level_advance;
            return level_advance > 0;
        } else {
            return false;
        }
    }

    public int getTotalExperience() {
        return experience;
    }

    public void setTotalExperience(int experience) {
        this.experience = experience;
        for (level = 0; level <= 3; level++) {
            if (experience < LEVEL_EXPERIENCE[level]) {
                level--;
                break;
            }
        }
        if (level > 3) {
            level = 3;
        }
    }

    public int getCurrentExperience() {
        return experience - LEVEL_EXPERIENCE[level];
    }

    public int getLevelUpExperience() {
        if (level < 3) {
            return LEVEL_EXPERIENCE[level + 1] - LEVEL_EXPERIENCE[level];
        } else {
            return -1;
        }
    }

    public void clearStatus() {
        this.status = null;
        resetMovementPoint();
    }

    public void attachStatus(Status status) {
        if ((getStatus() == null || getStatus().equals(status))
                && !hasAbility(Ability.HEAVY_MACHINE) && !isCrystal() && !getUnitCode().equals("saeth")) {
            setStatus(status);
        }
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void updateStatus() {
        if (status != null) {
            status.update();
            if (status.getRemainingTurn() < 0) {
                status = null;
            }
        }
    }

    public void setStatic(boolean is_static) {
        this.is_static = is_static;
    }

    public boolean isStatic() {
        return is_static;
    }

    public void setHead(int head) {
        this.head = head;
    }

    public int getHead() {
        return head;
    }

    public boolean isAt(int x, int y) {
        return this.x_position == x && this.y_position == y;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Unit) {
            Unit unit = (Unit) object;
            return getUnitCode().equals(unit.getUnitCode());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getUnitCode().hashCode();
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("index", getIndex());
        json.put("price_increment", price_increment);
        json.put("experience", getTotalExperience());
        json.put("unit_code", getUnitCode());
        json.put("team", getTeam());
        json.put("current_hp", getCurrentHP());
        json.put("current_movement_point", getCurrentMovementPoint());
        json.put("x_position", getX());
        json.put("y_position", getY());
        json.put("standby", isStandby());
        json.put("static", isStatic());
        json.put("head", getHead());
        if (getStatus() != null) {
            json.put("status", getStatus().toJson());
        }
        return json;
    }

    @Override
    public String getVerification() {
        String str = "";
        str = str
                + index
                + definition.price
                + definition.max_hp
                + definition.attack
                + definition.attack_type
                + definition.physical_defence
                + definition.magic_defence
                + definition.movement_point
                + definition.hp_growth
                + definition.attack_growth
                + definition.physical_defence_growth
                + definition.magic_defence_growth
                + definition.movement_growth
                + definition.max_attack_range
                + definition.min_attack_range;
        for (Integer ability : definition.abilities) {
            str += ability;
        }
        return str;
    }

    public static class Definition {

        public int price;

        public int occupancy;

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

        public Definition() {
            abilities = new Array<Integer>();
        }

    }

}
