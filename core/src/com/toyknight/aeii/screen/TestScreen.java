package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.toyknight.aeii.AEIIApplication;
import com.toyknight.aeii.AudioManager;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.entity.player.LocalPlayer;
import com.toyknight.aeii.entity.player.Player;
import com.toyknight.aeii.manager.LocalGameManager;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.rule.Rule;
import com.toyknight.aeii.screen.widgets.MapList;
import com.toyknight.aeii.utils.FileProvider;

/**
 * Created by toyknight on 6/21/2015.
 */
public class TestScreen extends Stage implements Screen {

    private final int ts;
    private final int sts;
    private final AEIIApplication context;

    private final SpriteBatch batch;

    private final MapList map_list;

    private TextButton btn_back;
    private TextButton btn_start;

    public TestScreen(AEIIApplication context) {
        this.context = context;
        this.batch = new SpriteBatch();
        this.ts = getContext().getTileSize();
        this.sts = ts / 24 * 10;
        this.btn_back = new TextButton("Back", getContext().getSkin());
        this.btn_back.setBounds(Gdx.graphics.getWidth() - ts * 9, ts / 2, ts * 4, ts);
        this.btn_back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().gotoMainMenuScreen();
            }
        });
        this.addActor(btn_back);
        this.btn_start = new TextButton("Start!", getContext().getSkin());
        this.btn_start.setBounds(Gdx.graphics.getWidth() - ts / 2 - ts * 4, ts / 2, ts * 4, ts);
        this.btn_start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Player[] players = new Player[4];
                for (int i = 0; i < 4; i++) {
                    if (map_list.getSelectedMap().getTeamAccess(i) == true) {
                        players[i] = new LocalPlayer();
                        players[i].setAlliance(i);
                        players[i].setGold(1000);
                    }
                }
                GameCore game = new GameCore(map_list.getSelectedMap(), Rule.getDefaultRule(), players);
                LocalGameManager manager = new LocalGameManager();
                manager.setGame(game);
                getContext().gotoGameScreen(manager);
            }
        });
        this.addActor(btn_start);

        this.map_list = new MapList(ts);
        ScrollPane sp_map_list = new ScrollPane(map_list);
        sp_map_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_map_list.setScrollBarPositions(false, true);
        sp_map_list.setBounds(ts / 2, ts / 2, ts * 8, Gdx.graphics.getHeight() - ts);
        this.addActor(sp_map_list);
    }

    private AEIIApplication getContext() {
        return context;
    }

    private void drawMapPreview() {
        int preview_width = Gdx.graphics.getWidth() - ts * 9 - ts / 2;
        int preview_height = Gdx.graphics.getHeight() - ts - ts / 2 * 3;
        //batch.draw(ResourceManager.getListBackground(), ts * 9, ts * 2, preview_width, preview_height);
        Map map = map_list.getSelectedMap();
        int map_offset_x = (preview_width - map.getWidth() * sts) / 2;
        int map_offset_y = (preview_height - map.getHeight() * sts) / 2;
        for (int map_x = 0; map_x < map.getWidth(); map_x++) {
            for (int map_y = 0; map_y < map.getHeight(); map_y++) {
                Tile tile = map.getTile(map_x, map_y);
                batch.draw(
                        ResourceManager.getSTileTexture(tile.getMiniMapIndex()),
                        ts * 9 + map_x * sts + map_offset_x, ts * 2 + preview_height - map_y * sts - sts - map_offset_y, sts, sts);
            }
        }
    }

    @Override
    public void draw() {
        batch.begin();
        batch.draw(ResourceManager.getPanelBackground(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        BorderRenderer.drawBorder(batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        drawMapPreview();
        batch.end();
        super.draw();
    }

    @Override
    public void show() {
        FileHandle[] internal_maps = FileProvider.getAssetsFile("map").list();
        FileHandle[] user_maps = FileProvider.getUserDir("map").list();
        FileHandle[] maps = new FileHandle[internal_maps.length + user_maps.length];
        System.arraycopy(internal_maps, 0, maps, 0, internal_maps.length);
        System.arraycopy(user_maps, 0, maps, internal_maps.length, user_maps.length);
        map_list.setMaps(maps);
        Gdx.input.setInputProcessor(this);
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
