package net.toyknight.aeii.campaign.warroom;

import net.toyknight.aeii.campaign.CampaignController;
import net.toyknight.aeii.system.AER;

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
        setOpen(true);
        addStage(new WarroomStage1());
        addStage(new WarroomStage2());
        addStage(new WarroomStage3());
        addStage(new WarroomStage4());
        addStage(new WarroomStage5());
    }

    @Override
    public String getCampaignName() {
        return AER.lang.getText("CAMPAIGN_WARROOM_NAME");
    }

    @Override
    public int getDifficulty() {
        return 3;
    }

}
