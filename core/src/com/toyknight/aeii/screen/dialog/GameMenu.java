package com.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.toyknight.aeii.AsyncTask;
import com.toyknight.aeii.entity.GameCore;
import com.toyknight.aeii.manager.GameManager;
import com.toyknight.aeii.screen.GameScreen;
import com.toyknight.aeii.utils.GameFactory;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.record.Recorder;

import java.io.IOException;

/**
 * @author toyknight 6/1/2015.
 */
public class GameMenu extends BasicDialog {

    private final int MARGIN;
    private final int BUTTON_WIDTH;
    private final int BUTTON_HEIGHT;
    private final int BUTTON_COUNT = 6;

    private TextButton btn_mini_map;
    private TextButton btn_objective;
    private TextButton btn_load;
    private TextButton btn_save;
    private TextButton btn_exit;
    private TextButton btn_resume;

    public GameMenu(GameScreen screen) {
        super(screen);
        this.MARGIN = ts / 4;
        this.BUTTON_WIDTH = ts * 4;
        this.BUTTON_HEIGHT = ts / 3 * 2;
        int menu_width = BUTTON_WIDTH + MARGIN * 2;
        int menu_height = BUTTON_HEIGHT * BUTTON_COUNT + MARGIN * (BUTTON_COUNT + 1);
        this.setBounds(
                (screen.getViewportWidth() - menu_width) / 2,
                (screen.getViewportHeight() - menu_height) / 2 + ts,
                menu_width, menu_height);
        this.initComponents();
    }

    private void initComponents() {
        this.btn_mini_map = new TextButton(Language.getText("LB_MINI_MAP"), getContext().getSkin());
        this.btn_mini_map.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().showDialog("map");
            }
        });
        this.add(btn_mini_map).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_objective = new TextButton(Language.getText("LB_OBJECTIVE"), getContext().getSkin());
        this.add(btn_objective).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_load = new TextButton(Language.getText("LB_LOAD"), getContext().getSkin());
        this.btn_load.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //screen.showLoadDialog();
            }
        });
        this.add(btn_load).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_save = new TextButton(Language.getText("LB_SAVE"), getContext().getSkin());
        this.btn_save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                trySaveGame();
            }
        });
        this.add(btn_save).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_exit = new TextButton(Language.getText("LB_EXIT_GAME"), getContext().getSkin());
        this.btn_exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getContext().submitAsyncTask(new AsyncTask<Void>() {
                    @Override
                    public Void doTask() throws Exception {
                        Recorder.saveRecord();
                        return null;
                    }

                    @Override
                    public void onFinish(Void result) {
                    }

                    @Override
                    public void onFail(String message) {
                    }
                });
                getContext().gotoStatisticsScreen(getOwner().getGame());
            }
        });
        this.add(btn_exit).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_resume = new TextButton(Language.getText("LB_RESUME"), getContext().getSkin());
        this.btn_resume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("menu");
            }
        });
        this.add(btn_resume).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
        this.layout();
    }

    @Override
    public GameScreen getOwner() {
        return (GameScreen) super.getOwner();
    }

    private void trySaveGame() {
        Gdx.input.setInputProcessor(null);
        btn_save.setText(Language.getText("LB_SAVING"));
        getContext().submitAsyncTask(new AsyncTask<Void>() {
            @Override
            public Void doTask() throws IOException {
                GameCore game = getOwner().getGame();
                GameFactory.save(game);
                return null;
            }

            @Override
            public void onFinish(Void result) {
                btn_save.setText(Language.getText("LB_SAVE"));
                getOwner().closeDialog("menu");
                getOwner().appendMessage(null, Language.getText("MSG_INFO_GSV"));
            }

            @Override
            public void onFail(String message) {
                System.out.println(message);
                btn_save.setText(Language.getText("LB_SAVE"));
                getOwner().closeDialog("menu");
                getOwner().appendMessage(null, Language.getText("MSG_INFO_GSVF"));
            }
        });
    }

    public void display() {
        btn_save.setVisible(canSave());
        btn_load.setVisible(!getContext().getNetworkManager().isConnected()
                && getOwner().getGame().getCurrentPlayer().isLocalPlayer());
    }

    private boolean canSave() {
        return getOwner().getGame().getCurrentPlayer().isLocalPlayer()
                && getOwner().getManager().getState() == GameManager.STATE_SELECT;
    }

}
