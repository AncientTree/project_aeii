package net.toyknight.aeii.campaign.warroom;

import net.toyknight.aeii.campaign.CampaignController;
import net.toyknight.aeii.utils.Language;

/**
 * @author blackwave 08/25/2016.
 */
public class WarroomCampaign extends CampaignController {

    @Override
    public String getCode() {
        return "C_WR";
    }

    @Override
    public void initialize() {
        addStage(new WarroomStage1());
        addStage(new WarroomStage2());
        addStage(new WarroomStage3());
    }

    @Override
    public String getCampaignName() {
        return Language.getText("CAMPAIGN_WARROOM_NAME");
    }

    @Override
    public int getDifficulty() {
        return 3;
    }

}
