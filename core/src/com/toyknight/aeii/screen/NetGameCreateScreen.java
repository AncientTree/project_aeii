package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.manager.GameHost;
import com.toyknight.aeii.net.NetworkTask;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.rule.Rule;
import com.toyknight.aeii.utils.Language;

/**
 * Created by toyknight on 8/28/2015.
 */
public class NetGameCreateScreen extends StageScreen {

    private TextButton btn_start;

    private long room_number;

    private String host;

    private Map map;
    private int[] player_type;
    private String[] team_allocation;
    private int[] alliance_state;

    private int initial_gold;
    private int max_population;

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

    public void setRoomNumber(long room_number) {
        this.room_number = room_number;
    }

    public long getRoomNumber() {
        return room_number;
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
            if (map.getTeamAccess(team)) {
                players[team] = new Player();
                players[team].setAlliance(alliance_state[team]);
                players[team].setGold(initial_gold);
                if (team_allocation[team].equals(getContext().getNetworkManager().getServiceName())) {
                    players[team].setType(player_type[team]);
                } else {
                    players[team].setType(Player.REMOTE);
                }
            }
        }
        Rule rule = Rule.getDefaultRule();
        rule.setMaxPopulation(max_population);
        GameCore game = new GameCore(map, rule, players);
        getContext().gotoGameScreen(game);
    }

    private void getRoomData() {
        getContext().getNetworkManager().postTask(new NetworkTask() {
            @Override
            public boolean doTask() throws Exception {
                host = getContext().getNetworkManager().requestHost();
                AEIIApplication.setButtonEnabled(btn_start, isHost());
                map = getContext().getNetworkManager().requestMap();
                player_type = getContext().getNetworkManager().requestPlayerType();
                team_allocation = getContext().getNetworkManager().requestTeamAllocation();
                alliance_state = getContext().getNetworkManager().requestAlliance();
                initial_gold = getContext().getNetworkManager().requestInitialGold();
                max_population = getContext().getNetworkManager().requestMaxPopulation();
                return true;
            }

            @Override
            public void onFinish() {
                Gdx.input.setInputProcessor(NetGameCreateScreen.this);
            }

            @Override
            public void onFail(String message) {
                getContext().showMessage(Language.getText("MSG_ERR_AEA"), null);
            }
        });
    }

    private boolean isHost() {
        return host.equals(getContext().getNetworkManager().getServiceName());
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
        Gdx.input.setInputProcessor(null);
        getContext().getNetworkManager().setNetworkListener(this);
        getRoomData();
    }

    @Override
    public void onGameStart() {
        createGame();
        System.out.println("game start");
    }

}
