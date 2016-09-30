package net.toyknight.aeii.campaign.aei;

import net.toyknight.aeii.campaign.CampaignController;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 7/25/2016.
 */
public class AEICampaign extends CampaignController {

    @Override
    public String getCode() {
        return "C_AEI";
    }

    @Override
    public void initialize() {
        addStage(new AEIStage1()).setRanking(false);
        addStage(new AEIStage2()).setRanking(false);
        addStage(new AEIStage3());
        addStage(new AEIStage4());
        addStage(new AEIStage5());
        addStage(new AEIStage6());
        addStage(new AEIStage7());
    }

    @Override
    public String getCampaignName() {
        return Language.getText("CAMPAIGN_AEI_NAME");
    }

    @Override
    public int getDifficulty() {
        return 1;
    }

}
