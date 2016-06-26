package net.toyknight.aeii.campaign.challenge;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Position;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/26/2016.
 */
public class ChallengeStage1 extends StageController {

    @Override
    public void onGameStart() {
        Message message = new Message(5, Language.getText("CAMPAIGN_CHALLENGE_STAGE_1_MESSAGE_1"));
        getContext().message(message);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {
    }

    @Override
    public void onUnitDestroyed(Unit unit) {
        if (unit.getTeam() == getPlayerTeam() && unit.isCommander()) {
            getContext().fail();
        } else {
            if (getContext().count(1) == 0) {
                getContext().clear();
            }
        }
    }

    @Override
    public void onTileRepaired(int x, int y) {
    }

    @Override
    public void onTileOccupied(int x, int y, int team) {
    }

    @Override
    public void onTurnStart(int turn) {
        switch (turn) {
            case 2:
                Array<Integer> indexes = new Array<Integer>();
                indexes.add(4);
                indexes.add(2);
                indexes.add(4);
                Array<Position> positions = new Array<Position>();
                positions.add(getContext().position(0, 1));
                positions.add(getContext().position(0, 0));
                positions.add(getContext().position(1, 0));
                getContext().reinforce(1, indexes, positions);
                indexes = new Array<Integer>();
                indexes.add(4);
                indexes.add(2);
                indexes.add(4);
                positions = new Array<Position>();
                positions.add(getContext().position(15, 16));
                positions.add(getContext().position(16, 16));
                positions.add(getContext().position(16, 15));
                getContext().reinforce(1, indexes, positions);
                break;
            case 4:
                indexes = new Array<Integer>();
                indexes.add(13);
                indexes.add(3);
                indexes.add(13);
                positions = new Array<Position>();
                positions.add(getContext().position(0, 7));
                positions.add(getContext().position(0, 8));
                positions.add(getContext().position(0, 9));
                getContext().reinforce(1, indexes, positions);
                indexes = new Array<Integer>();
                indexes.add(13);
                indexes.add(3);
                indexes.add(13);
                positions = new Array<Position>();
                positions.add(getContext().position(16, 7));
                positions.add(getContext().position(16, 8));
                positions.add(getContext().position(16, 9));
                getContext().reinforce(1, indexes, positions);
                break;
            case 6:
                indexes = new Array<Integer>();
                indexes.add(5);
                indexes.add(6);
                indexes.add(5);
                positions = new Array<Position>();
                positions.add(getContext().position(7, 0));
                positions.add(getContext().position(8, 0));
                positions.add(getContext().position(9, 0));
                getContext().reinforce(1, indexes, positions);
                indexes = new Array<Integer>();
                indexes.add(5);
                indexes.add(6);
                indexes.add(5);
                positions = new Array<Position>();
                positions.add(getContext().position(7, 16));
                positions.add(getContext().position(8, 16));
                positions.add(getContext().position(9, 16));
                getContext().reinforce(1, indexes, positions);
                break;
            case 8:
                indexes = new Array<Integer>();
                indexes.add(16);
                indexes.add(8);
                indexes.add(16);
                positions = new Array<Position>();
                positions.add(getContext().position(0, 7));
                positions.add(getContext().position(0, 8));
                positions.add(getContext().position(0, 9));
                getContext().reinforce(1, indexes, positions);
                indexes = new Array<Integer>();
                indexes.add(16);
                indexes.add(8);
                indexes.add(16);
                positions = new Array<Position>();
                positions.add(getContext().position(16, 7));
                positions.add(getContext().position(16, 8));
                positions.add(getContext().position(16, 9));
                getContext().reinforce(1, indexes, positions);
                break;
        }
    }

    @Override
    public void onTurnEnd(int turn) {
    }

    @Override
    public String getMapName() {
        return "challenge_stage_1.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        rule.setValue(Rule.Entry.MAX_POPULATION, 30);
        return rule;
    }

    @Override
    public int getStartGold() {
        return 0;
    }

    @Override
    public int getPlayerTeam() {
        return 0;
    }

    @Override
    public String[] getObjectives() {
        return new String[]{
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_1_OBJECTIVE_1"),
                Language.getText("CAMPAIGN_CHALLENGE_STAGE_1_OBJECTIVE_2"),
        };
    }

    @Override
    public int getStageNumber() {
        return 0;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_CHALLENGE_STAGE_1_NAME");
    }

}
