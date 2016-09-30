package net.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.concurrent.AsyncTask;
import net.toyknight.aeii.network.NetworkManager;
import net.toyknight.aeii.network.entity.LeaderboardRecord;
import net.toyknight.aeii.screen.StageScreen;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 9/20/2016.
 */
public class LeaderboardDialog extends BasicDialog {

    private final Label label_campaign_name;
    private final Label label_stage_name;

    private final Label label_online_turns;
    private final Label label_online_actions;

    private final Label label_local_turns;
    private final Label label_local_actions;

    private String campaign_code;
    private int stage_number;

    public LeaderboardDialog(StageScreen owner) {
        super(owner);
        label_campaign_name = new Label("", getContext().getSkin());
        label_campaign_name.setColor(getContext().getResources().getPhysicalAttackColor());
        label_campaign_name.setAlignment(Align.center);
        add(label_campaign_name).width(ts * 6).pad(ts / 4).row();
        label_stage_name = new Label("", getContext().getSkin());
        label_stage_name.setColor(90 / 256f, 150 / 256f, 77 / 256f, 1.0f);
        label_stage_name.setAlignment(Align.center);
        add(label_stage_name).width(ts * 6).pad(ts / 4).padTop(0).row();

        Label label_online_record = new Label(Language.getText("LB_ONLINE_RECORD"), getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };
        label_online_record.setAlignment(Align.center);
        add(label_online_record).size(ts * 6, ts).row();
        label_online_turns = new Label("", getContext().getSkin());
        add(label_online_turns).width(ts * 6).pad(ts / 4).row();
        label_online_actions = new Label("", getContext().getSkin());
        add(label_online_actions).width(ts * 6).pad(ts / 4).padTop(0).row();

        Label label_local_record = new Label(Language.getText("LB_LOCAL_RECORD"), getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY(), this.getWidth(), 1);
                batch.draw(getResources().getWhiteColor(), this.getX(), this.getY() + this.getHeight(), this.getWidth(), 1);
                super.draw(batch, parentAlpha);
            }
        };
        label_local_record.setAlignment(Align.center);
        add(label_local_record).size(ts * 6, ts).row();
        label_local_turns = new Label("", getContext().getSkin());
        add(label_local_turns).width(ts * 6).pad(ts / 4).row();
        label_local_actions = new Label("", getContext().getSkin());
        add(label_local_actions).width(ts * 6).pad(ts / 4).padTop(0).row();

        TextButton btn_ok = new TextButton(Language.getText("LB_OK"), getContext().getSkin());
        btn_ok.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });
        add(btn_ok).size(ts * 3, ts).padBottom(ts / 4);
        pack();
        setPosition((Gdx.graphics.getWidth() - getWidth()) / 2, (Gdx.graphics.getHeight() - getHeight()) / 2);
    }

    public void initialize(String campaign_code, int stage_number) {
        this.campaign_code = campaign_code;
        this.stage_number = stage_number;
        label_campaign_name.setText(
                "[" + getContext().getCampaignContext().getCampaign(campaign_code).getCampaignName() + "]");
        label_stage_name.setText(
                getContext().getCampaignContext().getCampaign(campaign_code).getStage(stage_number).getStageName());
    }

    private void tryLoadOnlineRecord() {
        getOwner().showPlaceholder(Language.getText("LB_CONNECTING"));
        getContext().submitAsyncTask(new AsyncTask<LeaderboardRecord>() {
            @Override
            public LeaderboardRecord doTask() throws Exception {
                if (NetworkManager.connect(GameContext.CAMPAIGN_SERVER)) {
                    return NetworkManager.requestBestRecord(campaign_code, stage_number);
                } else {
                    throw new AEIIException(Language.getText("MSG_ERR_CCS"));
                }
            }

            @Override
            public void onFinish(LeaderboardRecord result) {
                NetworkManager.disconnect();
                getOwner().closePlaceholder();
                if (result.getTurns() > 0) {
                    label_online_turns.setText(
                            Language.getText("LB_TURNS") + ": " + result.getTurns() + " [" + result.getUsernameTurns() + "]");
                } else {
                    label_online_turns.setText(Language.getText("LB_TURNS") + ": - [?]");
                }
                if (result.getActions() > 0) {
                    label_online_actions.setText(
                            Language.getText("LB_ACTIONS") + ": " + result.getActions() + " [" + result.getUsernameActions() + "]");
                } else {
                    label_online_actions.setText(Language.getText("LB_ACTIONS") + ": - [?]");
                }
            }

            @Override
            public void onFail(String message) {
                NetworkManager.disconnect();
                getOwner().closePlaceholder();
                getOwner().showNotification(message, null);
                label_online_turns.setText(Language.getText("LB_TURNS") + ": - [?]");
                label_online_actions.setText(Language.getText("LB_ACTIONS") + ": - [?]");
            }
        });
    }

    private void tryLoadLocalRecord() {
        int turn_record = getContext().getCampaignTurnRecord(campaign_code, stage_number);
        int action_record = getContext().getCampaignActionRecord(campaign_code, stage_number);
        label_local_turns.setText(Language.getText("LB_TURNS") + ": " + (turn_record > 0 ? turn_record : "-"));
        label_local_actions.setText(Language.getText("LB_ACTIONS") + ": " + (action_record > 0 ? action_record : "-"));
    }

    @Override
    public void display() {
        tryLoadLocalRecord();
        tryLoadOnlineRecord();
    }

}
