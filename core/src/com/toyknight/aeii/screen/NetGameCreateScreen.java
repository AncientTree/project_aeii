package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.net.task.NetworkTask;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.rule.Rule;
import com.toyknight.aeii.server.entity.RoomConfig;
import com.toyknight.aeii.utils.Language;

/**
 * Created by toyknight on 8/28/2015.
 */
public class NetGameCreateScreen extends StageScreen {

    private TextButton btn_start;

    private RoomConfig room_config;

    public NetGameCreateScreen(AEIIApplication context) {
        super(context);
        this.initComponents();
    }

    private void initComponents() {
        btn_start = new TextButton(Language.getText("LB_START"), getContext().getSkin());
        btn_start.setBounds(ts / 2, ts / 2, ts * 3, ts);
        btn_start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tryStartGame();
            }
        });
        addActor(btn_start);
    }

    public void setRoomConfig(RoomConfig config) {
        this.room_config = config;
    }

    public RoomConfig getRoomConfig() {
        return room_config;
    }

    private void tryStartGame() {
        //local check
        Gdx.input.setInputProcessor(null);
        btn_start.setText(Language.getText("LB_STARTING"));
        getContext().getNetworkManager().postTask(new NetworkTask() {
            @Override
            public boolean doTask() throws Exception {
                return getContext().getNetworkManager().requestStartGame();
            }

            @Override
            public void onFinish() {
                btn_start.setText(Language.getText("LB_START"));
                createGame();
            }

            @Override
            public void onFail(String message) {
                btn_start.setText(Language.getText("LB_START"));
                getContext().showMessage(message, null);
            }
        });
    }

    private void createGame() {
        GameHost.setHost(isHost());
        Player[] players = new Player[4];
        for (int team = 0; team < 4; team++) {
            if (room_config.map.getTeamAccess(team)) {
                players[team] = new Player();
                players[team].setAlliance(room_config.alliance_state[team]);
                players[team].setGold(room_config.initial_gold);
                if (getContext().getNetworkManager().getServiceName().equals(room_config.team_allocation[team])) {
                    players[team].setType(room_config.player_type[team]);
                } else {
                    players[team].setType(Player.REMOTE);
                }
            }
        }
        Rule rule = Rule.getDefaultRule();
        rule.setMaxPopulation(room_config.max_population);
        GameCore game = new GameCore(room_config.map, rule, players);
        getContext().gotoGameScreen(game);
    }

    private boolean isHost() {
        return getContext().getNetworkManager().getServiceName().equals(room_config.host);
    }

    @Override
    public void draw() {
        batch.begin();
        drawBackground();
        batch.end();
        super.draw();
    }

    private void drawBackground() {
        batch.draw(ResourceManager.getPanelBackground(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        BorderRenderer.drawBorder(batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        getContext().getNetworkManager().setNetworkListener(this);
    }

    @Override
    public void onGameStart() {
        createGame();
    }

}
