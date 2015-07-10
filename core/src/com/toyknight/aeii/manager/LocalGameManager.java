package com.toyknight.aeii.manager;

import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.manager.events.*;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.*;

/**
 * Created by toyknight on 4/4/2015.
 */
public class LocalGameManager extends GameManager {

    @Override
    public void beginMovePhase() {
        submitGameEvent(new PhaseBeginEvent(PhaseBeginEvent.PHASE_MOVE));
    }

    @Override
    public void cancelMovePhase() {
        submitGameEvent(new PhaseCancelEvent(PhaseCancelEvent.PHASE_MOVE));
    }

    @Override
    public void beginAttackPhase() {
        submitGameEvent(new PhaseBeginEvent(PhaseBeginEvent.PHASE_ATTACK));
    }

    @Override
    public void beginSummonPhase() {
        submitGameEvent(new PhaseBeginEvent(PhaseBeginEvent.PHASE_SUMMON));
    }

    @Override
    public void beginRemovePhase() {
        submitGameEvent(new PhaseBeginEvent(PhaseBeginEvent.PHASE_REMOVE));
    }

    @Override
    public void cancelActionPhase() {
        submitGameEvent(new PhaseCancelEvent(PhaseCancelEvent.PHASE_ACTION));
    }

    @Override
    public void selectUnit(int x, int y) {
        if (getState() == STATE_SELECT || getState() == STATE_PREVIEW) {
            Unit unit = getGame().getMap().getUnit(x, y);
            if (getGame().isUnitAccessible(unit)) {
                selected_unit = unit;
                submitGameEvent(new UnitSelectEvent(unit.getX(), unit.getY()));
            }
        }
    }

    @Override
    public void moveSelectedUnit(int dest_x, int dest_y) {
        Unit unit = getSelectedUnit();
        int start_x = unit.getX();
        int start_y = unit.getY();
        if (getMovablePositions().contains(getGame().getMap().getPosition(dest_x, dest_y))) {
            if (unit != null && getGame().isUnitAccessible(unit) &&
                    (getState() == STATE_MOVE || getState() == STATE_REMOVE) &&
                    canSelectedUnitMove(dest_x, dest_y)) {
                int mp_remains = getMovementPointRemains(dest_x, dest_y);
                ArrayList<Point> move_path = getMovePath(dest_x, dest_y);
                submitGameEvent(new UnitMoveEvent(start_x, start_y, dest_x, dest_y, mp_remains, move_path));
            }
        } else {
            cancelMovePhase();
        }
    }

    public void reverseMove() {
        Unit unit = getSelectedUnit();
        if (getGame().isUnitAccessible(unit) && getState() == STATE_ACTION) {
            submitGameEvent(new UnitMoveReverseEvent(unit.getX(), unit.getY(), last_position.x, last_position.y));
        }
    }

    @Override
    public void doAttack(int target_x, int target_y) {
        Unit attacker = getSelectedUnit();
        if (isActionPhase() && UnitToolkit.isWithinRange(attacker, target_x, target_y)) {
            Unit defender = getGame().getMap().getUnit(target_x, target_y);
            int kill_experience = getGame().getRule().getKillExperience();
            int attack_experience = getGame().getRule().getAttackExperience();
            int counter_experience = getGame().getRule().getCounterExperience();
            if (defender == null) {
                if (attacker.hasAbility(Ability.DESTROYER) && getGame().getMap().getTile(target_x, target_y).isDestroyable()) {
                    submitGameEvent(new UnitAttackEvent(attacker.getX(), attacker.getY(), target_x, target_y, -1, attack_experience));
                    submitGameEvent(new UnitStandbyEvent(attacker.getX(), attacker.getY()));
                    submitGameEvent(new TileDestroyEvent(target_x, target_y));
                    submitGameEvent(new UnitActionFinishEvent(attacker.getX(), attacker.getY()));
                }
            } else {
                if (getGame().isEnemy(attacker, defender)) {
                    //attack pre-calculation
                    attacker = UnitFactory.cloneUnit(attacker);
                    defender = UnitFactory.cloneUnit(defender);
                    int attack_damage = UnitToolkit.getDamage(attacker, defender, getGame().getMap());
                    UnitToolkit.attachAttackStatus(attacker, defender);
                    defender.changeCurrentHp(-attack_damage);
                    if (defender.getCurrentHp() > 0) {
                        attacker.gainExperience(attack_experience);
                        submitGameEvent(new UnitAttackEvent(attacker.getX(), attacker.getY(), defender.getX(), defender.getY(), attack_damage, attack_experience));
                        if (UnitToolkit.canCounter(defender, attacker)) {
                            int counter_damage = UnitToolkit.getDamage(defender, attacker, getGame().getMap());
                            attacker.changeCurrentHp(-counter_damage);
                            if (attacker.getCurrentHp() > 0) {
                                submitGameEvent(new UnitAttackEvent(defender.getX(), defender.getY(), attacker.getX(), attacker.getY(), counter_damage, counter_experience));
                            } else {
                                submitGameEvent(new UnitAttackEvent(defender.getX(), defender.getY(), attacker.getX(), attacker.getY(), counter_damage, kill_experience));
                            }
                        }
                    } else {
                        submitGameEvent(new UnitAttackEvent(attacker.getX(), attacker.getY(), defender.getX(), defender.getY(), attack_damage, kill_experience));
                    }
                    submitGameEvent(new UnitActionFinishEvent(attacker.getX(), attacker.getY()));
                }
            }
        }
    }

    @Override
    public void doSummon(int target_x, int target_y) {
        Unit summoner = getSelectedUnit();
        if (isActionPhase() && UnitToolkit.isWithinRange(summoner, target_x, target_y) && getGame().canSummon(target_x, target_y)) {
            int experience = getGame().getRule().getAttackExperience();
            submitGameEvent(new SummonEvent(summoner.getX(), summoner.getY(), target_x, target_y, experience));
            submitGameEvent(new UnitActionFinishEvent(summoner.getX(), summoner.getY()));
        }
    }

    @Override
    public void doRepair() {
        if (getState() == STATE_ACTION) {
            Unit unit = getSelectedUnit();
            submitGameEvent(new RepairEvent(unit.getX(), unit.getY()));
            submitGameEvent(new UnitActionFinishEvent(unit.getX(), unit.getY()));
        }
    }

    @Override
    public void doOccupy() {
        if (getState() == STATE_ACTION) {
            Unit unit = getSelectedUnit();
            submitGameEvent(new OccupyEvent(unit.getX(), unit.getY(), unit.getTeam()));
            submitGameEvent(new UnitActionFinishEvent(unit.getX(), unit.getY()));
        }
    }

    @Override
    public void buyUnit(String package_name, int index, int x, int y) {
        if (getGame().getMap().getUnit(x, y) == null) {
            int team = getGame().getCurrentTeam();
            submitGameEvent(new UnitBuyEvent(package_name, index, team, x, y, getGame().getUnitPrice(package_name, index, team)));
        }
    }

    @Override
    public void standbySelectedUnit() {
        if (getState() == STATE_ACTION) {
            Unit unit = getSelectedUnit();
            if (getGame().isUnitAccessible(unit)) {
                submitGameEvent(new UnitStandbyEvent(unit.getX(), unit.getY()));
            }
        }
    }

    @Override
    public void endCurrentTurn() {
        if (getState() == STATE_SELECT || getState() == STATE_PREVIEW) {
            submitGameEvent(new TurnEndEvent());
            //calculate hp change at turn start
            int team = getGame().getCurrentTeam();
            HashSet<Point> unit_position_set = new HashSet(getGame().getMap().getUnitPositionSet());
            HashMap<Point, Integer> hp_change_map = new HashMap();
            Set<Point> unit_position_set_copy = new HashSet(unit_position_set);
            for (Point position : unit_position_set_copy) {
                Unit unit = getGame().getMap().getUnit(position.x, position.y);
                if (unit.getTeam() == team) {
                    int change = 0;
                    //deal with terrain heal issues
                    change += UnitToolkit.getTerrainHeal(unit, getGame().getMap().getTile(unit.getX(), unit.getY()));
                    //deal with buff issues
                    if (unit.getStatus() != null && unit.getStatus().getType() == Status.POISONED) {
                        change -= getGame().getRule().getPoisonDamage();
                    }
                    hp_change_map.put(position, change);
                } else {
                    //remove other teams' unit position
                    unit_position_set.remove(position);
                }
            }
            //the healing aura
            for (Point position : unit_position_set) {
                Unit unit = getGame().getMap().getUnit(position.x, position.y);
                if (unit.hasAbility(Ability.HEALING_AURA)) {
                    for (int x = unit.getX() - 1; x <= unit.getX() + 1; x++) {
                        for (int y = unit.getY() - 1; y <= unit.getY() + 1; y++) {
                            //not healer himself
                            if ((x != unit.getX() || y != unit.getY()) && getGame().getMap().isWithinMap(x, y)) {
                                Point target_position = getGame().getMap().getPosition(x, y);
                                //there's a unit at the position
                                if (unit_position_set.contains(target_position)) {
                                    //see if this unit already has hp change
                                    if (hp_change_map.keySet().contains(target_position)) {
                                        int change = hp_change_map.get(target_position) + 15;
                                        hp_change_map.put(target_position, change);
                                    } else {
                                        hp_change_map.put(target_position, 15);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            submitGameEvent(new HpChangeEvent(hp_change_map));
            // pre-calculate unit that will be destroyed
            for (Point position : hp_change_map.keySet()) {
                Unit unit = getGame().getMap().getUnit(position.x, position.y);
                if (unit.getCurrentHp() + hp_change_map.get(position) <= 0) {
                    submitGameEvent(new UnitDestroyEvent(unit.getX(), unit.getY()));
                }
            }
            submitGameEvent(new UnitStatusUpdateEvent(team));
        }
    }

    public boolean canSelectedUnitMove(int dest_x, int dest_y) {
        Point dest = getGame().getMap().getPosition(dest_x, dest_y);
        return getMovablePositions().contains(dest) && getGame().canUnitMove(getSelectedUnit(), dest_x, dest_y);
    }

    public boolean canSelectUnitAct() {
        if (getSelectedUnit().hasAbility(Ability.SIEGE_MACHINE) && !getSelectedUnit().isAt(last_position.x, last_position.y)) {
            return false;
        } else {
            return true;
        }
    }

}
