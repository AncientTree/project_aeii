package net.toyknight.aeii.screen.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import net.toyknight.aeii.AEIIException;
import net.toyknight.aeii.AudioManager;
import net.toyknight.aeii.concurrent.AsyncTask;
import net.toyknight.aeii.entity.GameCore;
import net.toyknight.aeii.entity.Player;
import net.toyknight.aeii.manager.GameManager;
import net.toyknight.aeii.screen.GameScreen;
import net.toyknight.aeii.utils.GameToolkit;
import net.toyknight.aeii.utils.Language;

/**
 * @author toyknight 6/1/2015.
 */
public class GameMenu extends BasicDialog {

    private final int MARGIN;
    private final int BUTTON_WIDTH;
    private final int BUTTON_HEIGHT;
    private final int BUTTON_COUNT = 6;

    private TextButton btn_save;

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
        TextButton btn_mini_map = new TextButton(Language.getText("LB_MINI_MAP"), getContext().getSkin());
        btn_mini_map.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("menu");
                getOwner().showDialog("map");
            }
        });
        this.add(btn_mini_map).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        TextButton btn_objective = new TextButton(Language.getText("LB_OBJECTIVE"), getContext().getSkin());
        btn_objective.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("menu");
                getOwner().showDialog("objective");
            }
        });
        this.add(btn_objective).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        this.btn_save = new TextButton(Language.getText("LB_SAVE"), getContext().getSkin());
        this.btn_save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                trySaveGame();
            }
        });
        this.add(btn_save).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        TextButton btn_help = new TextButton(Language.getText("LB_HELP"), getContext().getSkin());
        btn_help.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getOwner().closeDialog("menu");
                getOwner().showWiki();
            }
        });
        this.add(btn_help).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        TextButton btn_exit = new TextButton(Language.getText("LB_EXIT_GAME"), getContext().getSkin());
        btn_exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (getContext().getGame().getType() == GameCore.CAMPAIGN) {
                    getContext().gotoCampaignScreen();
                    AudioManager.loopMainTheme();
                } else {
                    getContext().gotoStatisticsScreen(getOwner().getGame());
                }
            }
        });
        this.add(btn_exit).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(MARGIN).row();
        TextButton btn_resume = new TextButton(Language.getText("LB_RESUME"), getContext().getSkin());
        btn_resume.addListener(new ClickListener() {
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
            public Void doTask() throws AEIIException {
                GameCore game = getOwner().getGame();
                switch (game.getType()) {
                    case GameCore.SKIRMISH:
                        GameToolkit.saveSkirmish(game);
                        break;
                    case GameCore.CAMPAIGN:
                        String code = getContext().getCampaignContext().getCurrentCampaign().getCode();
                        int stage = getContext().getCampaignContext().getCurrentCampaign().getCurrentStage().getStageNumber();
                        ObjectMap<String, Integer> attributes = getContext().getCampaignContext().getCurrentCampaign().getAttributes();
                        GameToolkit.saveCampaign(game, code, stage, attributes);
                        break;
                }
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
                btn_save.setText(Language.getText("LB_SAVE"));
                getOwner().closeDialog("menu");
                getOwner().appendMessage(null, Language.getText("MSG_INFO_GSVF"));
            }
        });
    }

    public void display() {
        btn_save.setVisible(canSave());
    }

    private boolean canSave() {
        return getOwner().getGame().getCurrentPlayer().getType() == Player.LOCAL
                && getOwner().getGameManager().getState() == GameManager.STATE_SELECT;
    }

}
