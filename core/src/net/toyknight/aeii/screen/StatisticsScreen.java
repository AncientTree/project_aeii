package net.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import net.toyknight.aeii.AudioManager;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.concurrent.AsyncTask;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.entity.Statistics;
import net.toyknight.aeii.network.NetworkManager;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 9/22/2015.
 */
public class StatisticsScreen extends StageScreen {

    private Table header;
    private Table statistics_table;

    private Table[] team_table;
    private Label[] lb_income;
    private Label[] lb_destroy;
    private Label[] lb_lose;

    private TextButton btn_leave;

    public StatisticsScreen(GameContext context) {
        super(context);
        initComponents();
    }

    private void initComponents() {
        btn_leave = new TextButton(Language.getText("LB_LEAVE"), getContext().getSkin());
        btn_leave.setBounds(ts / 2, ts / 2, ts * 3, ts);
        btn_leave.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tryLeaveGame();
            }
        });
        addActor(btn_leave);

        header = new Table();
        Label lb_header_team = new Label(Language.getText("LB_TEAM"), getContext().getSkin());
        lb_header_team.setAlignment(Align.center);
        header.add(lb_header_team).size(ts * 2, ts);
        Label lb_header_income = new Label(Language.getText("LB_INCOME"), getContext().getSkin());
        lb_header_income.setAlignment(Align.center);
        header.add(lb_header_income).size(ts * 3, ts);
        Label lb_header_destroy = new Label(Language.getText("LB_DUV"), getContext().getSkin());
        lb_header_destroy.setAlignment(Align.center);
        header.add(lb_header_destroy).size(ts * 5, ts);
        Label lb_header_lose = new Label(Language.getText("LB_LUV"), getContext().getSkin());
        lb_header_lose.setAlignment(Align.center);
        header.add(lb_header_lose).size(ts * 5, ts);
        statistics_table = new Table();
        statistics_table.setBounds((Gdx.graphics.getWidth() - ts * 15) / 2, ts * 2, ts * 15, Gdx.graphics.getHeight() - ts * 4);
        addActor(statistics_table);

        team_table = new Table[4];
        Image[] team_color = new Image[4];
        lb_income = new Label[4];
        lb_destroy = new Label[4];
        lb_lose = new Label[4];
        for (int team = 0; team < 4; team++) {
            Texture color = getResources().getTeamBackground(team);
            team_color[team] = new Image(new TextureRegion(color, ts, ts));
            lb_income[team] = new Label("", getContext().getSkin());
            lb_income[team].setAlignment(Align.center);
            lb_destroy[team] = new Label("", getContext().getSkin());
            lb_destroy[team].setAlignment(Align.center);
            lb_lose[team] = new Label("", getContext().getSkin());
            lb_lose[team].setAlignment(Align.center);

            team_table[team] = new Table() {
                @Override
                public void draw(Batch batch, float parentAlpha) {
                    batch.draw(getResources().getBorderDarkColor(), getX(), getY(), getWidth(), getHeight());
                    super.draw(batch, parentAlpha);
                }
            };
            team_table[team].add(team_color[team]).size(ts * 2, ts);
            team_table[team].add(lb_income[team]).size(ts * 3, ts);
            team_table[team].add(lb_destroy[team]).size(ts * 5, ts);
            team_table[team].add(lb_lose[team]).size(ts * 5, ts);
        }
    }

    private void tryLeaveGame() {
        if (NetworkManager.isConnected()) {
            Gdx.input.setInputProcessor(null);
            btn_leave.setText(Language.getText("LB_LEAVING"));
            getContext().submitAsyncTask(new AsyncTask<Void>() {
                @Override
                public Void doTask() throws Exception {
                    NetworkManager.notifyLeaveRoom();
                    return null;
                }

                @Override
                public void onFinish(Void result) {
                    btn_leave.setText(Language.getText("LB_LEAVE"));
                    getContext().gotoLobbyScreen();
                    AudioManager.loopMainTheme();
                }

                @Override
                public void onFail(String message) {
                    btn_leave.setText(Language.getText("LB_LEAVE"));
                    showNotification(message, null);
                }
            });
        } else {
            getContext().gotoMainMenuScreen(true);
        }
    }

    public void setGame(GameCore game) {
        statistics_table.clearChildren();
        statistics_table.add(header).size(ts * 15, ts).row();

        Statistics statistics = game.getStatistics();
        if (statistics != null) {
            for (int team = 0; team < 4; team++) {
                if (game.getMap().hasTeamAccess(team)) {
                    lb_income[team].setText(Integer.toString(statistics.getIncome(team)));
                    lb_destroy[team].setText(Integer.toString(statistics.getDestroy(team)));
                    lb_lose[team].setText(Integer.toString(statistics.getLost(team)));
                    statistics_table.add(team_table[team]).size(ts * 15, ts).padTop(ts / 4).row();
                }
            }
        }
    }

    @Override
    public void draw() {
        batch.begin();
        batch.draw(getResources().getPanelBackground(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        getContext().getBorderRenderer().drawBorder(batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        getContext().getFontRenderer().drawTitleCenter(batch, Language.getText("LB_STATISTICS"),
                0, Gdx.graphics.getHeight() - ts * 2, Gdx.graphics.getWidth(), ts * 2);
        batch.end();
        super.draw();
    }

}
