package net.toyknight.aeii.campaign.aeiii;

import net.toyknight.aeii.campaign.CampaignController;
import net.toyknight.aeii.campaign.aeiii.AEIIIStage1;
import net.toyknight.aeii.system.AER;

/**
 * @author toyknight 6/28/2016.
 */
public class AEIIICampaign extends CampaignController {

    @Override
    public String getCode() {
        return "C_AEIII";
    }

    @Override
    public void initialize() {
        addStage(new AEIIIStage1()).setRanking(false);
        addStage(new AEIIIStage2());
        addStage(new AEIIIStage3());
        addStage(new AEIIIStage4());
        //addStage(new AEIIIStage5());
        //addStage(new AEIIIStage6());
        //addStage(new AEIIIStage7());
        //addStage(new AEIIIStage8());
    }

    @Override
    public String getCampaignName() {
        return AER.lang.getText("CAMPAIGN_AEIII_NAME");
    }

    @Override
    public int getDifficulty() {
        return 2;
    }

}
