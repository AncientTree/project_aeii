package net.toyknight.aeii.campaign.aei;

import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.campaign.Message;
import net.toyknight.aeii.campaign.Reinforcement;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.entity.Unit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 7/25/2016.
 */
public class AEIStage4 extends StageController {

    @Override
    public void onGameStart() {
        getContext().set("reinforced", 0);
        Message message1 = new Message(-1, Language.getText("CAMPAIGN_AEI_STAGE_4_MESSAGE_1"));
        getContext().message(message1);
        getContext().focus(2, 13);
        Message message2 = new Message(5, Language.getText("CAMPAIGN_AEI_STAGE_4_MESSAGE_2"));
        getContext().message(message2);
        getContext().focus(14, 13);
    }

    @Override
    public void onUnitMoved(Unit unit, int x, int y) {
    }

    @Override
    public void onUnitStandby(Unit unit) {
    }

    @Override
    public void onUnitAttacked(Unit attacker, Unit defender) {
    }

    @Override
    public void onUnitDestroyed(Unit unit) {
        if (getContext().get("reinforced") == 1 && getContext().count_unit(1) == 0) {
            Message message5 = new Message(1, Language.getText("CAMPAIGN_AEI_STAGE_4_MESSAGE_5"));
            getContext().message(message5);
            getContext().clear();
        }
        if (isCommander(unit, getPlayerTeam())) {
            getContext().fail();
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
        if (turn == 6) {
            getContext().set("reinforced", 1);
            getContext().reinforce(1,
                    new Reinforcement(9, 2, 2, 0),
                    new Reinforcement(5, 0, 0));
            Message message3 = new Message(0, Language.getText("CAMPAIGN_AEI_STAGE_4_MESSAGE_3"));
            Message message4 = new Message(1, Language.getText("CAMPAIGN_AEI_STAGE_4_MESSAGE_4"));
            getContext().message(message3, message4);
        }
    }

    @Override
    public String getMapName() {
        return "aei_c3.aem";
    }

    @Override
    public Rule getRule() {
        Rule rule = Rule.createDefault();
        Array<Integer> available_units = new Array<Integer>();
        available_units.add(0);
        available_units.add(1);
        available_units.add(2);
        available_units.add(3);
        available_units.add(4);
        available_units.add(5);
        rule.setAvailableUnits(available_units);
        rule.setValue(Rule.Entry.MAX_POPULATION, 15);
        return rule;
    }

    @Override
    public int getStartGold() {
        return 300;
    }

    @Override
    public int getPlayerTeam() {
        return 0;
    }

    @Override
    public String[] getObjectives() {
        return new String[]{Language.getText("CAMPAIGN_AEI_STAGE_4_OBJECTIVE")};
    }

    @Override
    public int getStageNumber() {
        return 3;
    }

    @Override
    public String getStageName() {
        return Language.getText("CAMPAIGN_AEI_STAGE_4_NAME");
    }

}
