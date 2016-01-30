package com.toyknight.aeii.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.AEIIException;
import com.toyknight.aeii.ResourceManager;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.entity.Map;
import com.toyknight.aeii.entity.Tile;
import com.toyknight.aeii.entity.Player;
import com.toyknight.aeii.renderer.BorderRenderer;
import com.toyknight.aeii.rule.Rule;
import com.toyknight.aeii.screen.widgets.StringList;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.MapFactory;
import com.toyknight.aeii.record.Recorder;

/**
 * @author toyknight 6/21/2015.
 */
public class TestScreen extends StageScreen implements StringList.SelectionListener {

    private final int sts;

    private final StringList<MapFactory.MapSnapshot> map_list;
    private Map map;

    public TestScreen(GameContext context) {
        super(context);
        this.sts = context.getTileSize() * 10 / 48;
        TextButton btn_back = new TextButton("Back", getContext().getSkin());
        btn_back.setBounds(Gdx.graphics.getWidth() - ts * 7, ts / 2, ts * 3, ts);
        btn_back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().gotoMainMenuScreen();
            }
        });
        this.addActor(btn_back);
        TextButton btn_start = new TextButton("Start!", getContext().getSkin());
        btn_start.setBounds(Gdx.graphics.getWidth() - ts / 2 - ts * 3, ts / 2, ts * 3, ts);
        btn_start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tryStartGame();
            }
        });
        this.addActor(btn_start);

        this.map_list = new StringList<MapFactory.MapSnapshot>(ts);
        this.map_list.setListener(this);
        ScrollPane sp_map_list = new ScrollPane(map_list, getContext().getSkin());
        sp_map_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(ResourceManager.getListBackground()));
        sp_map_list.setScrollBarPositions(false, true);
        sp_map_list.setBounds(ts / 2, ts / 2, ts * 8, Gdx.graphics.getHeight() - ts);
        this.addActor(sp_map_list);
    }

    private void drawMapPreview() {
        if (map != null) {
            int preview_width = Gdx.graphics.getWidth() - ts * 9 - ts / 2;
            int preview_height = Gdx.graphics.getHeight() - ts - ts / 2 * 3;
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
    }

    @Override
    public void onSelect(int index, Object value) {
        try {
            MapFactory.MapSnapshot snapshot = map_list.getSelected();
            map = MapFactory.createMap(snapshot.file);
        } catch (AEIIException ex) {
            map = null;
            getContext().showMessage(Language.getText("MSG_ERR_BMF"), null);
        }
    }

    private void tryStartGame() {
        if (map != null) {
            Recorder.setRecord(false);
            GameCore game = new GameCore(map, Rule.createDefault(), 1000, GameCore.SKIRMISH);
            boolean has_local_player = false;
            for (int team = 0; team < 4; team++) {
                if (game.getMap().hasTeamAccess(team)) {
                    if (has_local_player) {
                        game.getPlayer(team).setType(Player.ROBOT);
                        game.getPlayer(team).setAlliance(1);
                    } else {
                        game.getPlayer(team).setType(Player.LOCAL);
                        game.getPlayer(team).setAlliance(0);
                        has_local_player = true;
                    }
                }
            }
            getContext().gotoGameScreen(game);
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
        Gdx.input.setInputProcessor(this);
        Array<MapFactory.MapSnapshot> maps = MapFactory.getAllMapSnapshots();
        map_list.setItems(maps);
        onSelect(0, map_list.getSelected());
    }

}
