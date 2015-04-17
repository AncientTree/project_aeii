package com.toyknight.aeii.event;

import com.toyknight.aeii.entity.Ability;
import com.toyknight.aeii.entity.Buff;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Unit;
import com.toyknight.aeii.listener.GameListener;
import com.toyknight.aeii.utils.UnitToolkit;

/**
 *
 * @author toyknight
 */
public class UnitAttackEvent implements GameEvent {

	private final GameCore game;
	private final Unit attacker;
	private final Unit defender;

	public UnitAttackEvent(GameCore game, Unit attacker, Unit defender) {
		this.game = game;
		this.attacker = attacker;
		this.defender = defender;
	}

	protected GameCore getGame() {
		return game;
	}

	@Override
	public boolean canExecute() {
		return true;
	}

	@Override
	public void execute(GameListener listener) {
		int attack_damage = UnitToolkit.getDamage(attacker, defender, getGame().getMap());
		doDamage(attacker, defender, attack_damage, listener);
		int attack_exp = 30;
		if (defender.getCurrentHp() > 0) {
			if (UnitToolkit.canCounter(defender, attacker)) {
				int counter_damage = UnitToolkit.getDamage(defender, attacker, getGame().getMap());
				doDamage(defender, attacker, counter_damage, listener);
				int counter_exp = 20;
				if (attacker.getCurrentHp() > 0) {
					attachBuffAfterAttack(defender, attacker);
				} else {
					counter_exp += 30;
				}
				if (defender.gainExperience(counter_exp)) {
					listener.onUnitLevelUp(defender);
				}
			}
			attachBuffAfterAttack(attacker, defender);
		} else {
			attack_exp += 30;
		}
		if (attacker.getCurrentHp() > 0 && attacker.gainExperience(attack_exp)) {
			listener.onUnitLevelUp(attacker);
		}
	}

	private void doDamage(Unit attacker, Unit defender, int damage, GameListener listener) {
		damage = defender.getCurrentHp() > damage ? damage : defender.getCurrentHp();
		defender.setCurrentHp(defender.getCurrentHp() - damage);
		listener.onUnitAttack(attacker, defender, damage);
		if (defender.getCurrentHp() <= 0) {
			new UnitDestroyEvent(getGame(), defender).execute(listener);
		}
	}

	private void attachBuffAfterAttack(Unit attacker, Unit defender) {
		if (attacker.hasAbility(Ability.POISONER)) {
			defender.attachBuff(new Buff(Buff.POISONED, 2));
		}
	}

}
