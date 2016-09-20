package net.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.campaign.CampaignController;
import net.toyknight.aeii.campaign.StageController;
import net.toyknight.aeii.screen.dialog.CampaignModeDialog;
import net.toyknight.aeii.screen.widgets.StringList;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/25/2016.
 */
public class CampaignScreen extends StageScreen {

    private final StringList<CampaignController.Snapshot> scenario_list;
    private final ScrollPane sp_scenario_list;

    private final StringList<StageController.Snapshot> stage_list;
    private final ScrollPane sp_stage_list;

    private final Label label_difficulty;

    private final CampaignModeDialog campaign_mode_dialog;

    public CampaignScreen(GameContext context) {
        super(context);

        int list_width = (Gdx.graphics.getWidth() - ts / 2 * 3) / 2;

        Label label_scenarios = new Label(Language.getText("LB_SCENARIOS"), getContext().getSkin());
        label_scenarios.setAlignment(Align.center);
        label_scenarios.setBounds(ts / 2, Gdx.graphics.getHeight() - ts, list_width, ts);
        addActor(label_scenarios);

        scenario_list = new StringList<CampaignController.Snapshot>(getContext(), ts);
        scenario_list.setListener(new StringList.SelectionListener() {
            @Override
            public void onSelect(int index, Object value) {
                updateDifficulty();
                updateStages();
            }

            @Override
            public void onChange(int index, Object value) {
                updateDifficulty();
                updateStages();
            }
        });
        sp_scenario_list = new ScrollPane(scenario_list, getContext().getSkin());
        sp_scenario_list.setBounds(ts / 2, ts * 2, list_width, Gdx.graphics.getHeight() - ts * 3);
        addActor(sp_scenario_list);

        Label label_stages = new Label(Language.getText("LB_STAGES"), getContext().getSkin());
        label_stages.setAlignment(Align.center);
        label_stages.setBounds(list_width + ts, Gdx.graphics.getHeight() - ts, list_width, ts);
        addActor(label_stages);

        stage_list = new StringList<StageController.Snapshot>(getContext(), ts);
        sp_stage_list = new ScrollPane(stage_list, getContext().getSkin());
        sp_stage_list.setBounds(list_width + ts, ts * 2, list_width, Gdx.graphics.getHeight() - ts * 3);
        addActor(sp_stage_list);

        label_difficulty = new Label("", getContext().getSkin());
        label_difficulty.setAlignment(Align.center);
        label_difficulty.setBounds(0, 0, list_width, ts * 2);
        addActor(label_difficulty);

        Table button_bar = new Table();
        button_bar.setBounds(list_width + ts, 0, list_width, ts * 2);
        addActor(button_bar);

        TextButton btn_back = new TextButton(Language.getText("LB_BACK"), getContext().getSkin());
        btn_back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().gotoMainMenuScreen(false);
            }
        });
        button_bar.add(btn_back).size((list_width - ts / 2) / 2, ts);

        TextButton btn_start = new TextButton(Language.getText("LB_START"), getContext().getSkin());
        btn_start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                start();
            }
        });
        button_bar.add(btn_start).size((list_width - ts / 2) / 2, ts).padLeft(ts / 2);

        campaign_mode_dialog = new CampaignModeDialog(this);
        addDialog("mode", campaign_mode_dialog);
    }

    private void start() {
        CampaignController.Snapshot scenario_snapshot = scenario_list.getSelected();
        StageController.Snapshot stage_snapshot = stage_list.getSelected();
        if (scenario_snapshot != null && stage_snapshot != null) {
            if (scenario_snapshot.ranking) {
                campaign_mode_dialog.initialize(scenario_snapshot.code, stage_snapshot.stage);
                showDialog("mode");
            } else {
                getContext().gotoGameScreen(scenario_snapshot.code, stage_snapshot.stage);
            }
        }
    }

    private void updateScenarios() {
        Array<CampaignController.Snapshot> snapshots = new Array<CampaignController.Snapshot>();
        for (String code : getContext().getCampaignContext().getCampaignCodes()) {
            snapshots.add(getContext().getCampaignContext().getCampaign(code).createSnapshot());
        }
        snapshots.sort();
        scenario_list.setItems(snapshots);
    }

    private void updateStages() {
        CampaignController.Snapshot scenario_snapshot = scenario_list.getSelected();
        if (scenario_snapshot != null) {
            Array<StageController.Snapshot> snapshots = new Array<StageController.Snapshot>();
            Array<StageController> stages = getContext().getCampaignContext().getCampaign(scenario_snapshot.code).getStages();
            for (int i = 0; i <= getContext().getCampaignProgress(scenario_snapshot.code); i++) {
                snapshots.add(stages.get(i).createSnapshot());
            }
            stage_list.setItems(snapshots);
        }
    }

    private void updateDifficulty() {
        CampaignController.Snapshot scenario_snapshot = scenario_list.getSelected();
        if (scenario_snapshot == null) {
            label_difficulty.setText(Language.getText("LB_DIFFICULTY") + ": -");
        } else {
            label_difficulty.setText(Language.getText("LB_DIFFICULTY") +
                    ": " + Language.getText("LB_DIFFICULTY_" + scenario_snapshot.difficulty));
        }
    }

    @Override
    public void show() {
        super.show();
        updateScenarios();
        updateDifficulty();
        updateStages();
    }

    @Override
    public void draw() {
        batch.begin();
        batch.draw(getResources().getPanelBackground(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        getContext().getBorderRenderer().drawBorder(batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(
                getResources().getBorderDarkColor(),
                sp_scenario_list.getX() - ts / 24, sp_scenario_list.getY() - ts / 24,
                sp_scenario_list.getWidth() + ts / 12, sp_scenario_list.getHeight() + ts / 12);
        batch.draw(
                getResources().getBorderDarkColor(),
                sp_stage_list.getX() - ts / 24, sp_stage_list.getY() - ts / 24,
                sp_stage_list.getWidth() + ts / 12, sp_stage_list.getHeight() + ts / 12);
        batch.end();
        super.draw();
    }


}
