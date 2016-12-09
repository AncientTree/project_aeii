package net.toyknight.aeii.gui.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import net.toyknight.aeii.gui.StageScreen;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 9/20/2016.
 */
public class CampaignModeDialog extends BasicDialog {

    private String campaign_code;
    private int stage_number;

    public CampaignModeDialog(StageScreen owner) {
        super(owner);
        TextButton btn_normal = new TextButton(Language.getText("LB_NORMAL"), getContext().getSkin());
        btn_normal.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                startCampaign(campaign_code, stage_number, false);
            }
        });
        add(btn_normal).size(ts * 3, ts).pad(ts / 4).row();

        TextButton btn_ranking = new TextButton(Language.getText("LB_RANKING"), getContext().getSkin());
        btn_ranking.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                startCampaign(campaign_code, stage_number, true);
            }
        });
        add(btn_ranking).size(ts * 3, ts).pad(ts / 4).padTop(0).row();

        TextButton btn_cancel = new TextButton(Language.getText("LB_CANCEL"), getContext().getSkin());
        btn_cancel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });
        add(btn_cancel).size(ts * 3, ts).pad(ts / 4).padTop(0);
        pack();
        setPosition((Gdx.graphics.getWidth() - getWidth()) / 2, (Gdx.graphics.getHeight() - getHeight()) / 2);
    }

    public void initialize(String campaign_code, int stage_number) {
        this.campaign_code = campaign_code;
        this.stage_number = stage_number;
    }

    private void startCampaign(String campaign_code, int stage_number, boolean ranking) {
        close();
        getContext().gotoGameScreen(campaign_code, stage_number);
        getContext().getGameManager().setRanking(ranking);
        if (ranking) {
            getContext().getGame().getStatistics().resetActions();
        }
    }

}
