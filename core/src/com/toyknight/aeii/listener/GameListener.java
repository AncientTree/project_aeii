package com.toyknight.aeii.listener;

import com.toyknight.aeii.entity.Point;
import com.toyknight.aeii.entity.Unit;

import java.util.Map;

/**
 * Created by toyknight on 4/3/2015.
 */
public interface GameListener {

    public void onOccupy();

    public void onRepair();

    public void onSummon(Unit summoner, int target_x, int target_y);

    public void onUnitHpChange(Unit unit, int change);

    public void onMapHpChange(Map<Point, Integer> hp_change_map);

    public void onTileDestroy(int tile_index, int x, int y);

    public void onUnitAttack(Unit attacker, Unit defender, int damage);

    public void onUnitDestroy(Unit unit);

    public void onUnitLevelUp(Unit unit);

    public void onUnitMove(Unit unit, int start_x, int start_y, int dest_x, int dest_y);

    public void onUnitActionFinish(Unit unit);

    public void onUnitMoveFinish(Unit unit, int start_x, int start_y);

    public void onTurnStart(int turn, int income, int team);

    public void onGameOver(int team);

    public void onMapFocused(int map_x, int map_y);

    public void onGameEventCleared();

}
