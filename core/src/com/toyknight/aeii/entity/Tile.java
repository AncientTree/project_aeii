package com.toyknight.aeii.entity;

/**
 * @author toyknight 4/3/2015.
 */
public class Tile {

    public static final byte TYPE_LAND = 0;
    public static final byte TYPE_WATER = 1;
    public static final byte TYPE_FOREST = 2;
    public static final byte TYPE_MOUNTAIN = 3;

    private final int defence_bonus;
    private final int step_cost;
    private int hp_recovery = 0;
    private int type;
    private int top_tile_index = -1;
    private int mini_map_index = 0;

    private int team = -1;

    private int[] access_tile_list = null;

    private boolean can_be_captured = false;
    private boolean can_be_destroyed = false;
    private boolean can_be_repaired = false;
    private boolean is_animated = false;
    private boolean is_castle = false;
    private boolean is_village = false;

    private short[] captured_tile_list;
    private short destroyed_index;
    private short repaired_index;
    private short animation_tile_index;

    public Tile(int defence_bonus, int step_cost, int type) {
        this.defence_bonus = defence_bonus;
        this.step_cost = step_cost;
        this.type = type;
    }

    public int getDefenceBonus() {
        return defence_bonus;
    }

    public int getStepCost() {
        return step_cost;
    }

    public void setHpRecovery(int recovery) {
        this.hp_recovery = recovery;
    }

    public int getHpRecovery() {
        return hp_recovery;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setTopTileIndex(int index) {
        this.top_tile_index = index;
    }

    public int getTopTileIndex() {
        return top_tile_index;
    }

    public void setMiniMapIndex(int index) {
        this.mini_map_index = index;
    }

    public int getMiniMapIndex() {
        return mini_map_index;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public int getTeam() {
        return team;
    }

    public void setAccessTileList(int[] index) {
        this.access_tile_list = index;
    }

    public int[] getAccessTileList() {
        return access_tile_list;
    }

    public void setCapturable(boolean b) {
        this.can_be_captured = b;
    }

    public boolean isCapturable() {
        return can_be_captured;
    }

    public void setDestroyable(boolean b) {
        this.can_be_destroyed = b;
    }

    public boolean isDestroyable() {
        return can_be_destroyed;
    }

    public void setRepairable(boolean b) {
        this.can_be_repaired = b;
    }

    public boolean isRepairable() {
        return can_be_repaired;
    }

    public void setAnimated(boolean b) {
        this.is_animated = b;
    }

    public boolean isAnimated() {
        return is_animated;
    }

    public void setCastle(boolean b) {
        this.is_castle = b;
        if (is_village && is_castle) {
            this.is_village = false;
        }
    }

    public boolean isCastle() {
        return is_castle;
    }

    public void setVillage(boolean b) {
        this.is_village = b;
        if (is_village && is_castle) {
            this.is_castle = false;
        }
    }

    public boolean isVillage() {
        return is_village;
    }

    public void setCapturedTileList(short[] list) {
        this.captured_tile_list = list;
    }

    public short getCapturedTileIndex(int team) {
        if (0 <= team && team < 4) {
            return captured_tile_list[team];
        } else {
            return captured_tile_list[4];
        }
    }

    public void setDestroyedTileIndex(short index) {
        this.destroyed_index = index;
    }

    public short getDestroyedTileIndex() {
        return destroyed_index;
    }

    public void setRepairedTileIndex(short index) {
        this.repaired_index = index;
    }

    public short getRepairedTileIndex() {
        return repaired_index;
    }

    public void setAnimationTileIndex(short index) {
        this.animation_tile_index = index;

    }

    public short getAnimationTileIndex() {
        return animation_tile_index;
    }

    public String getVerificationString() {
        String str = "";
        str = str
                + defence_bonus
                + step_cost
                + hp_recovery
                + type
                + team
                + can_be_captured
                + can_be_destroyed
                + can_be_repaired
                + is_castle
                + is_village
                + destroyed_index
                + repaired_index;
        if (access_tile_list != null) {
            for (int index : access_tile_list) {
                str += index;
            }
        }
        if (captured_tile_list != null) {
            for (int index : captured_tile_list) {
                str += index;
            }
        }
        return str;
    }

}
