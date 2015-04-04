package com.toyknight.aeii.entity;

import com.toyknight.aeii.entity.player.Player;

import java.util.Collection;

/**
 * Created by toyknight on 4/3/2015.
 */
public class SkirmishGame extends BasicGame {

    private boolean is_game_over = false;

    public SkirmishGame(Map map, Player[] players, int max_population) {
        super(map, players, max_population);
    }

    protected void setGameOver(boolean b) {
        this.is_game_over = b;
    }

    public boolean isGameOver() {
        return is_game_over;
    }

    protected boolean checkEnemyUnitsCleaned(int team) {
        int current_alliance = getAlliance(getCurrentTeam());
        Collection<Unit> units = getMap().getUnitSet();
        for (Unit unit : units) {
            if (current_alliance != getAlliance(unit.getTeam())) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkEnemyCastlesCleaned(int team) {
        int current_alliance = getAlliance(getCurrentTeam());
        for (int x = 0; x < getMap().getWidth(); x++) {
            for (int y = 0; y < getMap().getHeight(); y++) {
                Tile tile = getMap().getTile(x, y);
                if (tile.isCastle() && current_alliance != getAlliance(tile.getTeam())) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void checkWinner() {
        int team = getCurrentTeam();
        if (checkEnemyUnitsCleaned(team) && checkEnemyCastlesCleaned(team)) {
            clearGameEvents();
            this.is_game_over = true;
            getGameListener().onGameOver(getAlliance(team));
        }
    }

    public void onUnitMoved(Unit unit, int dest_x, int dest_y) {
        //do nothing
    }

    public void onUnitDestroyed(Unit unit) {
        checkWinner();
    }

    public void onOccupy(int x, int y) {
        checkWinner();
    }

}
