package net.toyknight.aeii.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.GameException;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.entity.Map;
import net.toyknight.aeii.entity.Player;
import net.toyknight.aeii.entity.Rule;
import net.toyknight.aeii.gui.dialog.MiniMapDialog;
import net.toyknight.aeii.gui.widgets.Spinner;
import net.toyknight.aeii.gui.widgets.SpinnerListener;
import net.toyknight.aeii.gui.widgets.StringList;
import net.toyknight.aeii.renderer.BorderRenderer;
import net.toyknight.aeii.system.AER;
import net.toyknight.aeii.utils.MapFactory;
import net.toyknight.aeii.utils.TextureUtil;

/**
 * @author toyknight 6/21/2015.
 */
public class SkirmishGameCreateScreen extends StageScreen implements StringList.SelectionListener {

    private final StringList<MapFactory.MapSnapshot> map_list;
    private final ScrollPane sp_map_list;
    private final MiniMapDialog map_preview;

    private final Table team_setting_pane;
    private Image[] team_image;
    private Spinner<Integer>[] spinner_alliance;
    private Spinner<String>[] spinner_type;
    private Spinner<Integer> spinner_gold;
    private Spinner<Integer> spinner_population;

    private Map selected_map;

    public SkirmishGameCreateScreen(GameContext context) {
        super(context);

        int button_width = (Gdx.graphics.getWidth() - ts * 9) / 3;
        int map_list_width = Gdx.graphics.getWidth() - ts * 10 - ts / 2;

        TextButton btn_preview = new TextButton(AER.lang.getText("LB_PREVIEW"), getContext().getSkin());
        btn_preview.setBounds(ts * 7 + ts / 2, ts / 2, button_width, ts);
        btn_preview.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selected_map != null) {
                    showDialog("map");
                }
            }
        });
        this.addActor(btn_preview);
        TextButton btn_back = new TextButton(AER.lang.getText("LB_BACK"), getContext().getSkin());
        btn_back.setBounds(ts * 8 + button_width, ts / 2, button_width, ts);
        btn_back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().gotoMainMenuScreen(false);
            }
        });
        this.addActor(btn_back);
        TextButton btn_start = new TextButton(AER.lang.getText("LB_START"), getContext().getSkin());
        btn_start.setBounds(ts * 8 + ts / 2 + button_width * 2, ts / 2, button_width, ts);
        btn_start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tryStartGame();
            }
        });
        this.addActor(btn_start);

        this.map_list = new StringList<MapFactory.MapSnapshot>(ts);
        this.map_list.setListener(this);
        sp_map_list = new ScrollPane(map_list, getContext().getSkin()) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(
                        AER.resources.getBorderDarkColor(),
                        getX() - ts / 24, getY() - ts / 24, getWidth() + ts / 12, getHeight() + ts / 12);
                super.draw(batch, parentAlpha);
            }
        };
        sp_map_list.getStyle().background =
                new TextureRegionDrawable(new TextureRegion(AER.resources.getListBackground()));
        sp_map_list.setScrollBarPositions(false, true);
        sp_map_list.setBounds(ts / 2, ts / 2 + ts * 2, map_list_width, Gdx.graphics.getHeight() - ts * 3);
        this.addActor(sp_map_list);

        this.map_preview = new MiniMapDialog(this);
        this.map_preview.addClickListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeDialog("map");
            }
        });
        this.addDialog("map", map_preview);

        team_setting_pane = new Table();
        team_setting_pane.setBounds(map_list_width + ts / 2, ts * 2 + ts / 2, ts * 9 + ts / 2, ts * 6);
        addActor(team_setting_pane);

        team_image = new Image[4];
        spinner_alliance = new Spinner[4];
        spinner_type = new Spinner[4];
        Integer[] alliance_preset = new Integer[]{1, 2, 3, 4};
        String[] player_type_preset = new String[]{
                AER.lang.getText("LB_NONE"), AER.lang.getText("LB_PLAYER"), AER.lang.getText("LB_ROBOT")};
        for (int team = 0; team < 4; team++) {
            TextureRegionDrawable team_color =
                    TextureUtil.createDrawable(AER.resources.getTeamBackground(team), ts, ts);
            team_image[team] = new Image(team_color);

            spinner_alliance[team] = new Spinner<Integer>(getContext().getSkin());
            spinner_alliance[team].setListener(state_change_listener);
            spinner_alliance[team].setItems(alliance_preset);

            spinner_type[team] = new Spinner<String>(getContext().getSkin());
            spinner_type[team].setListener(state_change_listener);
            spinner_type[team].setContentWidth(ts * 2);
            spinner_type[team].setItems(player_type_preset);
        }

        Table gp_setting_pane = new Table();
        gp_setting_pane.setBounds(ts / 2, ts / 2, ts * 6 + ts / 2, ts * 2);
        addActor(gp_setting_pane);

        Label label_gold = new Label(AER.lang.getText("LB_START_GOLD"), getContext().getSkin());
        label_gold.setAlignment(Align.center);
        gp_setting_pane.add(label_gold).width(ts * 3).height(ts);
        Label label_population = new Label(AER.lang.getText("LB_MAX_POPULATION"), getContext().getSkin());
        label_population.setAlignment(Align.center);
        gp_setting_pane.add(label_population).width(ts * 3).height(ts).padLeft(ts / 2).row();
        spinner_gold = new Spinner<Integer>(getContext().getSkin());
        spinner_gold.setItems(Rule.GOLD_PRESET);
        spinner_gold.setListener(state_change_listener);
        gp_setting_pane.add(spinner_gold).width(ts * 3).height(ts);
        spinner_population = new Spinner<Integer>(getContext().getSkin());
        spinner_population.setItems(Rule.POPULATION_PRESET);
        spinner_population.setListener(state_change_listener);
        gp_setting_pane.add(spinner_population).width(ts * 3).height(ts).padLeft(ts / 2);

        Table top_pane = new Table();
        top_pane.setBounds(map_list_width + ts, ts * 8, ts * 9, Gdx.graphics.getHeight() - ts * 8);
        addActor(top_pane);

        Label label_empty = new Label("", getContext().getSkin());
        top_pane.add(label_empty).width(ts);
        Label label_team = new Label(AER.lang.getText("LB_TEAM"), getContext().getSkin());
        label_team.setAlignment(Align.center);
        top_pane.add(label_team).width(ts * 3).padLeft(ts / 2);
        Label label_type = new Label(AER.lang.getText("LB_TYPE"), getContext().getSkin());
        label_type.setAlignment(Align.center);
        top_pane.add(label_type).width(ts * 4).padLeft(ts / 2);
    }

    @Override
    public void onChange(int index, Object value) {
        try {
            team_setting_pane.clear();

            MapFactory.MapSnapshot snapshot = map_list.getSelected();
            selected_map = MapFactory.createMap(snapshot.file);
            map_preview.setMap(selected_map);
            map_preview.updateBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            for (int team = 0; team < 4; team++) {
                if (selected_map.hasTeamAccess(team)) {
                    team_setting_pane.add(team_image[team])
                            .size(ts, ts)
                            .padTop(ts / 2)
                            .padLeft(ts / 2);
                    team_setting_pane.add(spinner_alliance[team])
                            .size(ts * 3, ts)
                            .padTop(ts / 2)
                            .padLeft(ts / 2);
                    team_setting_pane.add(spinner_type[team])
                            .size(ts * 4, ts)
                            .padTop(ts / 2)
                            .padLeft(ts / 2).row();
                }
                spinner_gold.setSelectedIndex(0);
                spinner_population.setSelectedIndex(0);
                GameCore game = new GameCore(selected_map, Rule.createDefault(), Rule.GOLD_PRESET[0], GameCore.SKIRMISH);
                getContext().getRoomManager().setGame(game);
                updateView();
            }
        } catch (GameException ex) {
            selected_map = null;
            showNotification(AER.lang.getText("MSG_ERR_BMF"), null);
        }
    }

    @Override
    public void onSelect(int index, Object value) {
    }

    private void onStateChange() {
        for (int team = 0; team < 4; team++) {
            if (getContext().getRoomManager().getGame().getMap().hasTeamAccess(team)) {
                String selected = spinner_type[team].getSelectedItem();
                if (selected.equals(AER.lang.getText("LB_NONE"))) {
                    getContext().getRoomManager().updatePlayerType(team, Player.NONE);
                }
                if (selected.equals(AER.lang.getText("LB_PLAYER"))) {
                    getContext().getRoomManager().updatePlayerType(team, Player.LOCAL);
                }
                if (selected.equals(AER.lang.getText("LB_ROBOT"))) {
                    getContext().getRoomManager().updatePlayerType(team, Player.ROBOT);
                }
                int alliance = spinner_alliance[team].getSelectedItem();
                getContext().getRoomManager().updateAlliance(team, alliance);
                getContext().getRoomManager().updateStartGold(spinner_gold.getSelectedItem());
                getContext().getRoomManager().updateMaxPopulation(spinner_population.getSelectedItem());
            }
        }
    }

    private void updateView() {
        for (int team = 0; team < 4; team++) {
            if (getContext().getRoomManager().getGame().getMap().hasTeamAccess(team)) {
                switch (getContext().getRoomManager().getGame().getPlayer(team).getType()) {
                    case Player.NONE:
                        spinner_type[team].setSelectedIndex(0);
                        break;
                    case Player.LOCAL:
                        spinner_type[team].setSelectedIndex(1);
                        break;
                    case Player.ROBOT:
                        spinner_type[team].setSelectedIndex(2);
                        break;
                }
                spinner_alliance[team].setSelectedIndex(
                        getContext().getRoomManager().getGame().getPlayer(team).getAlliance() - 1);
            }
        }
    }

    private boolean canStart() {
        int player_count = 0;
        int alliance = -1;
        boolean alliance_ready = false;
        GameCore game = getContext().getRoomManager().getGame();
        for (int team = 0; team < 4; team++) {
            if (game.getMap().hasTeamAccess(team) && game.getPlayer(team).getType() != Player.NONE) {
                player_count++;
                if (alliance == -1) {
                    alliance = game.getPlayer(team).getAlliance();
                } else {
                    if (alliance != game.getPlayer(team).getAlliance()) {
                        alliance_ready = true;
                    }
                }
            }
        }
        return player_count >= 2 && alliance_ready;
    }

    private void tryStartGame() {
        if (selected_map != null && canStart()) {
            getContext().gotoGameScreen(getContext().getRoomManager().getArrangedGame());
        } else {
            showNotification(AER.lang.getText("MSG_ERR_CNSG"), null);
        }
    }

    @Override
    public void draw() {
        batch.begin();
        batch.draw(AER.resources.getPanelBackground(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        BorderRenderer.drawBorder(batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if (selected_map == null) {
            AER.font.drawTextCenter(batch,
                    AER.lang.getText("MSG_ERR_BMF"),
                    ts * 7, ts * 2,
                    Gdx.graphics.getWidth() - ts * 7 - ts / 2, Gdx.graphics.getHeight() - ts * 2 - ts / 2);
        }
        batch.end();
        super.draw();
    }

    @Override
    public void act(float delta) {
        map_preview.update(delta);
        super.act(delta);
    }

    @Override
    public void show() {
        super.show();
        Array<MapFactory.MapSnapshot> maps = MapFactory.getAllMapSnapshots();
        map_list.setItems(maps);
        sp_map_list.layout();
        if (maps.size > 0) {
            onChange(0, map_list.getSelected());
        }
    }

    private final SpinnerListener state_change_listener = new SpinnerListener() {
        @Override
        public void onValueChanged(Spinner spinner) {
            onStateChange();
        }
    };

}
