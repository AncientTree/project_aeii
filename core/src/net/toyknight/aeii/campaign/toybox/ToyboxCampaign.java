package net.toyknight.aeii.campaign.toybox;

import net.toyknight.aeii.campaign.CampaignController;
import net.toyknight.aeii.system.AER;

/**
 * @author blackwave 08/25/2016.
 */
public class ToyboxCampaign extends CampaignController {


    @Override
    public String getCode() {
        return "C_TB";
    }

    @Override
    public void initialize() {
        setOpen(true);
        addStage(new ToyboxStage1()).setRanking(false);
    }

    @Override
    public String getCampaignName() {
        return AER.lang.getText("CAMPAIGN_TOYBOX_NAME");
    }

    @Override
    public int getDifficulty() {
        return 1;
    }
}
