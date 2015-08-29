package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.DialogCallback;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.net.NetworkListener;
import com.toyknight.aeii.net.NetworkTask;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.utils.Language;

/**
 * Created by toyknight on 8/28/2015.
 */
public class NetGameCreateScreen extends Stage implements Screen, NetworkListener {

    private final AEIIApplication context;
    private final SpriteBatch batch;

    private long room_number;

    private String host;

    private Map map;
    private int[] player_type;
    private String[] team_allocation;
    private int[] alliance_state;

    private int initial_gold;
    private int max_population;

    public NetGameCreateScreen(AEIIApplication context) {
        this.context = context;
        this.batch = new SpriteBatch();
    }

    public AEIIApplication getContext() {
        return context;
    }

    public void setRoomNumber(long room_number) {
        this.room_number = room_number;
    }

    public long getRoomNumber() {
        return room_number;
    }

    private void getRoomData() {
        getContext().getNetworkManager().postTask(new NetworkTask() {
            @Override
            public boolean doTask() throws Exception {
                host = getContext().getNetworkManager().requestHost(room_number);
//                System.out.println("host: "+host);
                map = getContext().getNetworkManager().requestMap(room_number);
//                System.out.println("map: "+map.getAuthor());
                player_type = getContext().getNetworkManager().requestPlayerType(room_number);
//                System.out.println("pt: "+player_type.length);
                team_allocation = getContext().getNetworkManager().requestTeamAllocation(room_number);
//                System.out.println("ta: "+team_allocation.length);
                alliance_state = getContext().getNetworkManager().requestAlliance(room_number);
//                System.out.println("as: "+alliance_state.length);
                initial_gold = getContext().getNetworkManager().requestInitialGold(room_number);
//                System.out.println("gold: "+initial_gold);
                max_population = getContext().getNetworkManager().requestMaxPopulation(room_number);
//                System.out.println("pop: "+max_population);
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

    @Override
    public void onDisconnect() {
        getContext().showMessage(Language.getText("MSG_ERR_DFS"), new DialogCallback() {
            @Override
            public void doCallback() {
                getContext().gotoMainMenuScreen();
            }
        });
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
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        getContext().getNetworkManager().setNetworkListener(this);
        getRoomData();
    }

    @Override
    public void render(float delta) {
        this.draw();
        this.act(delta);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}
